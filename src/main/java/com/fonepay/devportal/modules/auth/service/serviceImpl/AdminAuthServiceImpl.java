package com.fonepay.devportal.modules.auth.service.serviceImpl;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fonepay.devportal.common.constant.enums.AuthStatus;
import com.fonepay.devportal.common.constant.enums.TokenType;
import com.fonepay.devportal.common.constant.enums.UserStatus;
import com.fonepay.devportal.common.exception.ForbiddenException;
import com.fonepay.devportal.common.exception.InvalidOtpException;
import com.fonepay.devportal.common.exception.UnauthorizedException;
import com.fonepay.devportal.modules.auth.document.UserToken;
import com.fonepay.devportal.modules.auth.dto.request.LoginRequest;
import com.fonepay.devportal.modules.auth.dto.request.OtpVerifyRequest;
import com.fonepay.devportal.modules.auth.dto.response.AuthResponse;
import com.fonepay.devportal.modules.auth.dto.response.OtpResponse;
import com.fonepay.devportal.modules.auth.mapper.AuthMapper;
import com.fonepay.devportal.modules.auth.repository.UserTokenRepository;
import com.fonepay.devportal.modules.auth.service.AdminAuthService;
import com.fonepay.devportal.modules.auth.service.OtpService;
import com.fonepay.devportal.modules.auth.service.UserTokenService;
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
    private final UserTokenService userTokenService;
    private final UserTokenRepository userTokenRepository;
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
        boolean isAdmin = roleNames.stream()
                .anyMatch(r -> r.equalsIgnoreCase(ROLE_ADMIN));
        if (!isAdmin) {
            log.warn("User {} attempted admin login without ADMIN role", user.getUserId());
            throw new ForbiddenException("Access denied: User is not an administrator");
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
    public OtpResponse setupOtp(String tokenId) {
        UserToken token = userTokenRepository.findByIdAndTokenType(tokenId, TokenType.LOGIN_OTP)
                .orElseThrow(() -> new UnauthorizedException("Invalid or expired login session"));

        if (token.getUsedAt() != null) {
            throw new UnauthorizedException("Invalid or expired login session");
        }

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        List<String> roleNames = userRoleService.getRoleNamesByUserId(user.getUserId());
        boolean isPrivileged = roleNames.stream()
                .map(String::toUpperCase)
                .anyMatch(ROLES_EDITOR::contains);
        if (!isPrivileged) {
            throw new ForbiddenException("OTP setup is restricted to Admin/Editor roles");
        }

        Instant now = clock.instant();
        if (token.getExpiresAt() != null && token.getExpiresAt().isBefore(now)) {
            userTokenService.deleteToken(token);
            throw new InvalidOtpException("OTP session expired. Please log in again.");
        }

        String otpCode = otpService.generateOtpCode();
        String otpHash = userTokenService.hashToken(otpCode);

        token.setTokenHash(otpHash);
        token.setAttempts(0);
        token.setExpiresAt(now.plusSeconds(otpExpirationMinutes * 60L));
        token.setUsedAt(null);
        userTokenRepository.save(token);

        emailService.sendOtpEmail(user.getEmail(), otpCode, user.getFullName());
        log.info("Admin/Editor OTP resent for login token: {}", tokenId);

        return authMapper.toOtpResponse(
                "OTP sent to your email",
                (int) java.time.Duration.between(now, token.getExpiresAt()).getSeconds());
    }

    @Override
    public AuthResponse verifyAdminOtp(String tokenId, OtpVerifyRequest request) {
        return completeOtpVerification(tokenId, request, Set.of(ROLE_ADMIN));
    }

    @Override
    public AuthResponse verifyEditorOtp(String tokenId, OtpVerifyRequest request) {
        return completeOtpVerification(tokenId, request, ROLES_EDITOR);
    }

    private User validateCredentials(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Password mismatch for: {}", request.getEmail());
            throw new UnauthorizedException("Invalid email or password");
        }

        if (user.getStatus() == UserStatus.DEACTIVATED || user.getStatus() == UserStatus.INACTIVE) {
            log.warn("Login attempt for deactivated/inactive user: {}", user.getUserId());
            throw new UnauthorizedException("Account is deactivated. Please contact support.");
        }

        user.setLastLoginAt(clock.instant());
        userRepository.save(user);
        return user;
    }

    private AuthResponse initiateAdminOtpFlow(User user) {
        String otpCode = otpService.generateOtpCode();
        String otpHash = userTokenService.hashToken(otpCode);

        UserToken token = userTokenService.createLoginOtpToken(user.getUserId(), otpHash, otpExpirationMinutes);
        emailService.sendOtpEmail(user.getEmail(), otpCode, user.getFullName());

        log.info("Initiated Admin/Editor OTP for user: {} with login token: {}", user.getUserId(), token.getId());
        return authMapper.toAuthResponse(user, token.getId(), AuthStatus.OTP_REQUIRED);
    }

    private AuthResponse completeOtpVerification(String tokenId, OtpVerifyRequest request, Set<String> requiredRoles) {
        UserToken token = userTokenRepository.findByIdAndTokenType(tokenId, TokenType.LOGIN_OTP)
                .orElseThrow(() -> new UnauthorizedException("Invalid or expired login session"));

        if (token.getUsedAt() != null) {
            throw new UnauthorizedException("Invalid or expired login session");
        }

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        List<String> roleNames = userRoleService.getRoleNamesByUserId(user.getUserId());
        boolean hasRequiredRole = roleNames.stream()
                .map(String::toUpperCase)
                .anyMatch(requiredRoles::contains);
        if (!hasRequiredRole) {
            log.warn("User {} lacking required role {} for verification", user.getUserId(), requiredRoles);
            throw new ForbiddenException("Access denied: User does not have required permissions");
        }

        Instant now = clock.instant();
        if (token.getExpiresAt() != null && token.getExpiresAt().isBefore(now)) {
            userTokenService.deleteToken(token);
            throw new InvalidOtpException("OTP has expired. Please log in again.");
        }

        boolean verified = userTokenService.verifyLoginOtp(token, request.getCode(), MAX_OTP_ATTEMPTS);

        if (!verified) {
            if (token.getAttempts() >= MAX_OTP_ATTEMPTS) {
                userTokenService.deleteToken(token);
                throw new InvalidOtpException("Max OTP attempts exceeded. Please log in again.");
            } else {
                int remainingAttempts = MAX_OTP_ATTEMPTS - token.getAttempts();
                throw new InvalidOtpException("Invalid OTP code. " + remainingAttempts + " attempts remaining.");
            }
        }

        userTokenService.deleteToken(token);
        log.info("Admin/Editor OTP verification successful for user: {}", user.getUserId());

        UserSession session = userSessionService.createSession(user.getUserId(), null, null, jwtExpirationMs);
        String jwt = jwtUtil.generateToken(user, session.getSessionId(), roleNames);
        return authMapper.toAuthResponse(user, jwt, roleNames, AuthStatus.LOGIN_SUCCESS);
    }
}
