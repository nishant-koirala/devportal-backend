package com.fonepay.devportal.modules.auth.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fonepay.devportal.common.constant.enums.AuthStatus;
import com.fonepay.devportal.common.constant.enums.PendingAuthStatus;
import com.fonepay.devportal.common.constant.enums.TokenType;
import com.fonepay.devportal.common.constant.enums.UserStatus;
import com.fonepay.devportal.common.exception.BadRequestException;
import com.fonepay.devportal.common.exception.EmailAlreadyVerifiedException;
import com.fonepay.devportal.common.exception.ForbiddenException;
import com.fonepay.devportal.common.exception.InvalidOtpException;
import com.fonepay.devportal.common.exception.ResourceNotFoundException;
import com.fonepay.devportal.common.exception.UnauthorizedException;
import com.fonepay.devportal.common.exception.UserAlreadyExistsException;
import com.fonepay.devportal.common.util.IdGenerator;
import com.fonepay.devportal.modules.auth.document.PendingAuth;
import com.fonepay.devportal.modules.auth.document.UserToken;
import com.fonepay.devportal.modules.auth.repository.PendingAuthRepository;
import com.fonepay.devportal.modules.auth.dto.request.ForgotPasswordRequest;
import com.fonepay.devportal.modules.auth.dto.request.LoginRequest;
import com.fonepay.devportal.modules.auth.dto.request.OtpVerifyRequest;
import com.fonepay.devportal.modules.auth.dto.request.RegisterRequest;
import com.fonepay.devportal.modules.auth.dto.request.ResetPasswordRequest;
import com.fonepay.devportal.modules.auth.dto.response.AuthResponse;
import com.fonepay.devportal.modules.auth.dto.response.OtpResponse;
import com.fonepay.devportal.modules.auth.dto.response.RegistrationResponse;
import com.fonepay.devportal.modules.auth.mapper.AuthMapper;
import com.fonepay.devportal.modules.auth.policy.MfaPolicy;
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
public class AuthServiceImpl implements AuthService {

    private static final String DEFAULT_ROLE = "ADMIN";
    private static final long EMAIL_VERIFICATION_TOKEN_HOURS = 24;
    private static final long PASSWORD_RESET_TOKEN_HOURS = 1;
    private static final long RESEND_COOLDOWN_SECONDS = 60;
    private static final int MAX_OTP_ATTEMPTS = 3;

    private final UserRepository userRepository;
    private final UserRoleService userRoleService;
    private final UserSessionService userSessionService;
    private final UserTokenService userTokenService;
    private final MfaPolicy mfaPolicy;
    private final OtpService otpService;
    private final PendingAuthService pendingAuthService;
    private final PendingAuthRepository pendingAuthRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthMapper authMapper;
    private final Clock clock;

    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Value("${FRONTEND_URL}")
    private String frontendUrl;

    // ==========================================
    // UserRegistrationService Implementation
    // ==========================================

    @Override
    public RegistrationResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User already exists with email: " + request.getEmail());
        }

        Instant now = Instant.now(clock);
        User user = new User();
        user.setUserId(IdGenerator.nextUlid());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setCompanyName(request.getCompanyName());
        user.setStatus(UserStatus.PENDING);
        user.setEmailVerified(false);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        user = userRepository.save(user);

        // Assign default role
        userRoleService.assignDefaultRole(user.getUserId(), DEFAULT_ROLE);

        // Create and send verification token
        String rawToken = userTokenService.createAndSaveToken(
                user.getUserId(), TokenType.EMAIL_VERIFICATION, EMAIL_VERIFICATION_TOKEN_HOURS);
        sendVerificationEmail(user.getEmail(), rawToken);

        return authMapper.toRegistrationResponse(user);
    }

    @Override
    public void verifyEmail(String rawToken) {
        UserToken token = userTokenService.validateAndConsumeToken(rawToken, TokenType.EMAIL_VERIFICATION);

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.isEmailVerified()) {
            throw new EmailAlreadyVerifiedException("Email is already verified");
        }

        user.setEmailVerified(true);
        user.setStatus(UserStatus.ACTIVE);
        user.setUpdatedAt(Instant.now(clock));
        userRepository.save(user);
    }

    @Override
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.isEmailVerified()) {
            throw new EmailAlreadyVerifiedException("Email is already verified");
        }

        userTokenService.checkRateLimit(user.getUserId(), TokenType.EMAIL_VERIFICATION, RESEND_COOLDOWN_SECONDS);

        String rawToken = userTokenService.createAndSaveToken(
                user.getUserId(), TokenType.EMAIL_VERIFICATION, EMAIL_VERIFICATION_TOKEN_HOURS);
        sendVerificationEmail(user.getEmail(), rawToken);
    }

    // ==========================================
    // AuthenticationService Implementation
    // ==========================================

    @Override
    public AuthResponse login(LoginRequest request, String ipAddress, String userAgent) {
        log.info("Processing login request for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Password mismatch for email: {}", request.getEmail());
            throw new UnauthorizedException("Invalid email or password");
        }

        if (user.getStatus() == UserStatus.DEACTIVATED) {
            log.warn("Login attempt for deactivated user: {}", user.getUserId());
            throw new UnauthorizedException("Account is deactivated. Please contact support.");
        }

        Instant now = clock.instant();
        user.setLastLoginAt(now);
        userRepository.save(user);

        List<String> roleNames = userRoleService.getRoleNamesByUserId(user.getUserId());
        boolean requiresMfa = mfaPolicy.isMfaRequired(user, roleNames);

        if (requiresMfa) {
            // Generate OTP code (plain text for email)
            String otpCode = otpService.generateOtpCode();
            // Hash OTP for secure storage in pending auth
            String otpHash = otpService.hashOtp(otpCode);

            // Create pending auth record with OTP hash (NO session created yet)
            // OTP expiration is 5 minutes (300 seconds) - use otpExpirationMinutes from OtpService
            int otpExpirationMinutes = 5; // Default, matches OtpService default
            PendingAuth pendingAuth = pendingAuthService.createPendingAuth(user.getUserId(), otpHash, otpExpirationMinutes);

            // Send OTP via email
            emailService.sendOtpEmail(user.getEmail(), otpCode, user.getFullName());

            // Return pending auth ID as reference (not session ID or JWT)
            return authMapper.toAuthResponse(user, pendingAuth.getId(), AuthStatus.OTP_REQUIRED);
        }

        // No MFA required - create session and issue JWT immediately
        UserSession session = userSessionService.createSession(user.getUserId(), ipAddress, userAgent, jwtExpirationMs);
        String token = jwtUtil.generateToken(user, session.getSessionId(), roleNames);
        return authMapper.toAuthResponse(user, token, roleNames, AuthStatus.LOGIN_SUCCESS);
    }

    @Override
    public void logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }

        String token = authHeader.substring(7);
        String sessionId = jwtUtil.extractSessionId(token);

        if (sessionId != null) {
            userSessionService.revokeSessionBySessionId(sessionId);
        }
    }

    @Override
    public String extractUserIdFromToken(String token) {
        try {
            return jwtUtil.extractUserId(token);
        } catch (Exception e) {
            log.error("Failed to extract user ID from token", e);
            return null;
        }
    }

    // ==========================================
    // PasswordService Implementation
    // ==========================================

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        userRepository.findByEmail(email).ifPresent(user -> {
            if (user.getStatus() == UserStatus.DEACTIVATED) {
                return;
            }

            userTokenService.checkRateLimit(user.getUserId(), TokenType.PASSWORD_RESET, RESEND_COOLDOWN_SECONDS);

            String rawToken = userTokenService.createAndSaveToken(
                    user.getUserId(), TokenType.PASSWORD_RESET, PASSWORD_RESET_TOKEN_HOURS);
            String resetUrl = frontendUrl + "/reset-password?token=" + rawToken;
            emailService.sendPasswordResetEmail(user.getEmail(), resetUrl);
        });
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirm password do not match");
        }

        UserToken token = userTokenService.validateAndConsumeToken(request.getToken(), TokenType.PASSWORD_RESET);

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Instant now = Instant.now(clock);
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(now);
        userRepository.save(user);

        // Revoke all active sessions on password change
        userSessionService.revokeAllActiveSessions(user.getUserId());
    }

    // ==========================================
    // MfaAuthService Implementation
    // ==========================================

    @Override
    public OtpResponse requestOtp(String pendingAuthId) {
        PendingAuth pendingAuth = pendingAuthService.findById(pendingAuthId)
                .orElseThrow(() -> new UnauthorizedException("Invalid or expired pending authentication"));

        // Check if pending auth belongs to user and is still pending
        if (pendingAuth.getStatus() != PendingAuthStatus.PENDING) {
            throw new UnauthorizedException("Invalid or expired pending authentication");
        }

        User user = userRepository.findById(pendingAuth.getUserId())
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        List<String> roleNames = userRoleService.getRoleNamesByUserId(user.getUserId());
        if (!mfaPolicy.isMfaRequired(user, roleNames)) {
            throw new ForbiddenException("OTP not required for this user role");
        }

        // Check if expired
        Instant now = Instant.now(clock);
        if (pendingAuth.getExpiresAt() != null && pendingAuth.getExpiresAt().isBefore(now)) {
            pendingAuth.setStatus(PendingAuthStatus.EXPIRED);
            pendingAuthService.deletePendingAuth(pendingAuth);
            throw new InvalidOtpException("OTP has expired. Please request a new one by logging in again.");
        }

        // Generate new OTP
        String otpCode = otpService.generateOtpCode();
        String otpHash = otpService.hashOtp(otpCode);

        // Update pending auth with new OTP hash and reset attempts
        pendingAuth.setOtpHash(otpHash);
        pendingAuth.setAttempts(0);
        pendingAuth.setExpiresAt(now.plusSeconds(5 * 60L)); // 5 minutes
        pendingAuth.setStatus(PendingAuthStatus.PENDING);
        pendingAuth.setVerifiedAt(null);
        pendingAuthRepository.save(pendingAuth); // Save the updated pending auth

        emailService.sendOtpEmail(user.getEmail(), otpCode, user.getFullName());
        log.info("OTP resent for pending auth: {}", pendingAuthId);

        return authMapper.toOtpResponse(
                "OTP sent to your email",
                (int) java.time.Duration.between(now, pendingAuth.getExpiresAt()).getSeconds());
    }

    @Override
    public AuthResponse verifyOtp(String pendingAuthId, OtpVerifyRequest request) {
        PendingAuth pendingAuth = pendingAuthService.findById(pendingAuthId)
                .orElseThrow(() -> new UnauthorizedException("Invalid or expired pending authentication"));

        if (pendingAuth.getStatus() != PendingAuthStatus.PENDING) {
            throw new UnauthorizedException("Invalid or expired pending authentication");
        }

        User user = userRepository.findById(pendingAuth.getUserId())
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        List<String> roleNames = userRoleService.getRoleNamesByUserId(user.getUserId());
        if (!mfaPolicy.isMfaRequired(user, roleNames)) {
            throw new ForbiddenException("OTP verification not required for this user");
        }

        // Verify OTP using pending auth service
        boolean verified = pendingAuthService.verifyOtp(pendingAuth, request.getCode(), MAX_OTP_ATTEMPTS);

        if (!verified) {
            // Check the status to provide appropriate error message
            if (pendingAuth.getStatus() == PendingAuthStatus.EXPIRED) {
                pendingAuthService.deletePendingAuth(pendingAuth);
                throw new InvalidOtpException("OTP has expired. Please request a new one by logging in again.");
            } else if (pendingAuth.getStatus() == PendingAuthStatus.FAILED) {
                pendingAuthService.deletePendingAuth(pendingAuth);
                throw new InvalidOtpException("Max OTP attempts exceeded. Please request a new OTP by logging in again.");
            } else {
                int remainingAttempts = MAX_OTP_ATTEMPTS - pendingAuth.getAttempts();
                throw new InvalidOtpException("Invalid OTP code. " + remainingAttempts + " attempts remaining.");
            }
        }

        // OTP verified successfully - create ACTIVE session now
        pendingAuthService.deletePendingAuth(pendingAuth);
        log.info("OTP verification successful for user: {}", user.getUserId());

        // Create session and issue JWT
        UserSession session = userSessionService.createSession(user.getUserId(), "unknown", "unknown", jwtExpirationMs);
        String token = jwtUtil.generateToken(user, session.getSessionId(), roleNames);
        return authMapper.toAuthResponse(user, token, roleNames, AuthStatus.LOGIN_SUCCESS);
    }

    // ==========================================
    // Private Helpers
    // ==========================================

    private void sendVerificationEmail(String email, String rawToken) {
        String verificationUrl = frontendUrl + "/verify-email?token=" + rawToken;
        emailService.sendVerificationEmail(email, verificationUrl);
    }
}
