package com.fonepay.devportal.modules.auth.service.serviceImpl;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fonepay.devportal.common.constant.enums.AuthStatus;
import com.fonepay.devportal.common.constant.enums.PendingAuthStatus;
import com.fonepay.devportal.common.constant.enums.UserStatus;
import com.fonepay.devportal.common.exception.ForbiddenException;
import com.fonepay.devportal.common.exception.InvalidOtpException;
import com.fonepay.devportal.common.exception.UnauthorizedException;
import com.fonepay.devportal.modules.auth.document.PendingAuth;
import com.fonepay.devportal.modules.auth.dto.request.LoginRequest;
import com.fonepay.devportal.modules.auth.dto.request.OtpVerifyRequest;
import com.fonepay.devportal.modules.auth.dto.response.AuthResponse;
import com.fonepay.devportal.modules.auth.dto.response.OtpResponse;
import com.fonepay.devportal.modules.auth.mapper.AuthMapper;
import com.fonepay.devportal.modules.auth.repository.PendingAuthRepository;
import com.fonepay.devportal.modules.auth.service.AdminAuthService;
import com.fonepay.devportal.modules.auth.service.OtpService;
import com.fonepay.devportal.modules.auth.service.PendingAuthService;
import com.fonepay.devportal.modules.notification.service.EmailService;
import com.fonepay.devportal.modules.user.document.User;
import com.fonepay.devportal.modules.user.document.UserSession;
import com.fonepay.devportal.modules.user.repository.UserRepository;
import com.fonepay.devportal.modules.user.service.UserRoleService;
import com.fonepay.devportal.modules.user.service.UserSessionService;
import com.fonepay.devportal.security.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuthServiceImpl implements AdminAuthService {

    private static final int MAX_OTP_ATTEMPTS = 3;
    private static final String ROLE_ADMIN = "ADMIN";
    private static final Set<String> ROLES_EDITOR = Set.of("ADMIN", "EDITOR");

    private final UserRepository userRepository;
    private final UserRoleService userRoleService;
    private final UserSessionService userSessionService;
    private final PendingAuthService pendingAuthService;
    private final PendingAuthRepository pendingAuthRepository;
    private final OtpService otpService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthMapper authMapper;
    private final Clock clock;

    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Value("${app.otp.expiration-minutes:5}")
    private int otpExpirationMinutes;

    @Override
    public AuthResponse adminLogin(LoginRequest request, String ipAddress, String userAgent) {
        log.info("Processing admin login request for: {}", request.getEmail());
        User user = validateCredentials(request);

        List<String> roleNames = userRoleService.getRoleNamesByUserId(user.getUserId());
        boolean isAdmin = roleNames.stream().anyMatch(ROLE_ADMIN::equalsIgnoreCase);
        if (!isAdmin) {
            log.warn("User {} attempted admin login without ADMIN role", user.getUserId());
            throw new ForbiddenException("Access denied: User does not have ADMIN privileges");
        }

        return initiateAdminOtpFlow(user);
    }

    @Override
    public AuthResponse editorLogin(LoginRequest request, String ipAddress, String userAgent) {
        log.info("Processing editor login request for: {}", request.getEmail());
        User user = validateCredentials(request);

        List<String> roleNames = userRoleService.getRoleNamesByUserId(user.getUserId());
        boolean isEditorOrAdmin = roleNames.stream()
                .map(String::toUpperCase)
                .anyMatch(ROLES_EDITOR::contains);
        if (!isEditorOrAdmin) {
            log.warn("User {} attempted editor login without EDITOR or ADMIN role", user.getUserId());
            throw new ForbiddenException("Access denied: User does not have EDITOR or ADMIN privileges");
        }

        return initiateAdminOtpFlow(user);
    }

    @Override
    public OtpResponse setupOtp(String pendingAuthId) {
        PendingAuth pendingAuth = pendingAuthService.findById(pendingAuthId)
                .orElseThrow(() -> new UnauthorizedException("Invalid or expired pending authentication"));

        if (pendingAuth.getStatus() != PendingAuthStatus.PENDING) {
            throw new UnauthorizedException("Invalid or expired pending authentication");
        }

        User user = userRepository.findById(pendingAuth.getUserId())
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        List<String> roleNames = userRoleService.getRoleNamesByUserId(user.getUserId());
        boolean isPrivileged = roleNames.stream()
                .map(String::toUpperCase)
                .anyMatch(ROLES_EDITOR::contains);
        if (!isPrivileged) {
            throw new ForbiddenException("OTP setup is restricted to Admin/Editor roles");
        }

        Instant now = clock.instant();
        if (pendingAuth.getExpiresAt() != null && pendingAuth.getExpiresAt().isBefore(now)) {
            pendingAuth.setStatus(PendingAuthStatus.EXPIRED);
            pendingAuthService.deletePendingAuth(pendingAuth);
            throw new InvalidOtpException("OTP session expired. Please log in again.");
        }

        String otpCode = otpService.generateOtpCode();
        String otpHash = otpService.hashOtp(otpCode);

        pendingAuth.setOtpHash(otpHash);
        pendingAuth.setAttempts(0);
        pendingAuth.setExpiresAt(now.plusSeconds(otpExpirationMinutes * 60L));
        pendingAuth.setStatus(PendingAuthStatus.PENDING);
        pendingAuth.setVerifiedAt(null);
        pendingAuthRepository.save(pendingAuth);

        emailService.sendOtpEmail(user.getEmail(), otpCode, user.getFullName());
        log.info("Admin/Editor OTP resent for pending auth: {}", pendingAuthId);

        return authMapper.toOtpResponse(
                "OTP sent to your email",
                (int) java.time.Duration.between(now, pendingAuth.getExpiresAt()).getSeconds());
    }

    @Override
    public AuthResponse verifyAdminOtp(String pendingAuthId, OtpVerifyRequest request) {
        return completeOtpVerification(pendingAuthId, request, Set.of(ROLE_ADMIN));
    }

    @Override
    public AuthResponse verifyEditorOtp(String pendingAuthId, OtpVerifyRequest request) {
        return completeOtpVerification(pendingAuthId, request, ROLES_EDITOR);
    }

    private User validateCredentials(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Password mismatch for: {}", request.getEmail());
            throw new UnauthorizedException("Invalid email or password");
        }

        if (user.getStatus() == UserStatus.DEACTIVATED) {
            log.warn("Login attempt for deactivated user: {}", user.getUserId());
            throw new UnauthorizedException("Account is deactivated. Please contact support.");
        }

        user.setLastLoginAt(clock.instant());
        userRepository.save(user);
        return user;
    }

    private AuthResponse initiateAdminOtpFlow(User user) {
        String otpCode = otpService.generateOtpCode();
        String otpHash = otpService.hashOtp(otpCode);

        PendingAuth pendingAuth = pendingAuthService.createPendingAuth(user.getUserId(), otpHash, otpExpirationMinutes);
        emailService.sendOtpEmail(user.getEmail(), otpCode, user.getFullName());

        log.info("Initiated Admin/Editor OTP for user: {} with pendingAuth: {}", user.getUserId(), pendingAuth.getId());
        return authMapper.toAuthResponse(user, pendingAuth.getId(), AuthStatus.OTP_REQUIRED);
    }

    private AuthResponse completeOtpVerification(String pendingAuthId, OtpVerifyRequest request, Set<String> requiredRoles) {
        PendingAuth pendingAuth = pendingAuthService.findById(pendingAuthId)
                .orElseThrow(() -> new UnauthorizedException("Invalid or expired pending authentication"));

        if (pendingAuth.getStatus() != PendingAuthStatus.PENDING) {
            throw new UnauthorizedException("Invalid or expired pending authentication");
        }

        User user = userRepository.findById(pendingAuth.getUserId())
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        List<String> roleNames = userRoleService.getRoleNamesByUserId(user.getUserId());
        boolean hasRequiredRole = roleNames.stream()
                .map(String::toUpperCase)
                .anyMatch(requiredRoles::contains);
        if (!hasRequiredRole) {
            log.warn("User {} lacking required role {} for verification", user.getUserId(), requiredRoles);
            throw new ForbiddenException("Access denied: User does not have required permissions");
        }

        boolean verified = pendingAuthService.verifyOtp(pendingAuth, request.getCode(), MAX_OTP_ATTEMPTS);

        if (!verified) {
            if (pendingAuth.getStatus() == PendingAuthStatus.EXPIRED) {
                pendingAuthService.deletePendingAuth(pendingAuth);
                throw new InvalidOtpException("OTP has expired. Please log in again.");
            } else if (pendingAuth.getStatus() == PendingAuthStatus.FAILED) {
                pendingAuthService.deletePendingAuth(pendingAuth);
                throw new InvalidOtpException("Max OTP attempts exceeded. Please log in again.");
            } else {
                int remainingAttempts = MAX_OTP_ATTEMPTS - pendingAuth.getAttempts();
                throw new InvalidOtpException("Invalid OTP code. " + remainingAttempts + " attempts remaining.");
            }
        }

        pendingAuthService.deletePendingAuth(pendingAuth);
        log.info("Admin/Editor OTP verification successful for user: {}", user.getUserId());

        UserSession session = userSessionService.createSession(user.getUserId(), null, null, jwtExpirationMs);
        String token = jwtUtil.generateToken(user, session.getSessionId(), roleNames);
        return authMapper.toAuthResponse(user, token, roleNames, AuthStatus.LOGIN_SUCCESS);
    }
}
