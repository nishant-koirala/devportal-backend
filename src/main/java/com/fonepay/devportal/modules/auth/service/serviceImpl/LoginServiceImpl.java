package com.fonepay.devportal.modules.auth.service.serviceImpl;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fonepay.devportal.common.constant.AuthMessages;
import com.fonepay.devportal.common.constant.enums.ActivityType;
import com.fonepay.devportal.common.constant.enums.AuthStatus;
import com.fonepay.devportal.common.constant.enums.UserStatus;
import com.fonepay.devportal.common.exception.UnauthorizedException;
import com.fonepay.devportal.modules.admin.developer.service.ActivityRecordingService;
import com.fonepay.devportal.modules.auth.document.UserToken;
import com.fonepay.devportal.modules.auth.dto.request.LoginRequest;
import com.fonepay.devportal.modules.auth.dto.response.AuthResponse;
import com.fonepay.devportal.modules.auth.mapper.AuthMapper;
import com.fonepay.devportal.modules.auth.policy.MfaPolicy;
import com.fonepay.devportal.modules.auth.service.LoginService;
import com.fonepay.devportal.modules.auth.service.OtpService;
import com.fonepay.devportal.modules.auth.service.UserTokenService;
import com.fonepay.devportal.modules.notification.service.EmailService;
import com.fonepay.devportal.modules.user.document.User;
import com.fonepay.devportal.modules.user.document.UserSession;
import com.fonepay.devportal.modules.user.repository.UserRepository;
import com.fonepay.devportal.modules.user.service.UserRoleService;
import com.fonepay.devportal.modules.user.service.UserSessionService;
import com.fonepay.devportal.security.JwtUtil;
import com.fonepay.devportal.security.RateLimitService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final UserRepository userRepository;
    private final UserRoleService userRoleService;
    private final UserSessionService userSessionService;
    private final MfaPolicy mfaPolicy;
    private final OtpService otpService;
    private final UserTokenService userTokenService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthMapper authMapper;
    private final Clock clock;
    private final ActivityRecordingService activityRecordingService;
    private final RateLimitService rateLimitService;

    @Value("${app.otp.expiration-minutes:5}")
    private int otpExpirationMinutes;

    @Override
    public AuthResponse login(LoginRequest request, String ipAddress, String userAgent) {
        log.info("Processing login request for: {}", request.getEmail());
        rateLimitService.checkAuthEmail(request.getEmail());

        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException(AuthMessages.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Password mismatch for: {}", request.getEmail());
            activityRecordingService.recordLogin(user.getUserId(), ipAddress, userAgent, false);
            throw new UnauthorizedException(AuthMessages.INVALID_CREDENTIALS);
        }

        if (user.getStatus() == UserStatus.DEACTIVATED) {
            log.warn("Login attempt for deactivated user: {}", user.getUserId());
            activityRecordingService.recordLogin(user.getUserId(), ipAddress, userAgent, false);
            throw new UnauthorizedException(AuthMessages.DEACTIVATED);
        }

        if (!user.isEmailVerified() || user.getStatus() == UserStatus.PENDING) {
            log.warn("Login attempt for unverified user: {}", user.getUserId());
            activityRecordingService.recordLogin(user.getUserId(), ipAddress, userAgent, false);
            throw new UnauthorizedException(AuthMessages.unverified(user.getEmail()));
        }

        Instant now = clock.instant();
        user.setLastLoginAt(now);
        userRepository.save(user);

        List<String> roleNames = userRoleService.getRoleNamesByUserId(user.getUserId());
        boolean requiresMfa = mfaPolicy.isMfaRequired(user, roleNames);

        if (requiresMfa) {
            // Generate OTP code and hash
            String otpCode = otpService.generateOtpCode();
            String otpHash = userTokenService.hashToken(otpCode);

            // Create UserToken record with LOGIN_OTP type
            UserToken token = userTokenService.createLoginOtpToken(user.getUserId(), otpHash, otpExpirationMinutes);

            // Send OTP email
            emailService.sendOtpEmail(user.getEmail(), otpCode, user.getFullName());

            // Return tokenId with OTP_REQUIRED status
            return authMapper.toAuthResponse(user, token.getId(), AuthStatus.OTP_REQUIRED);
        }

        // Non-MFA: create session and issue JWT immediately
        UserSession session = userSessionService.createSession(user.getUserId(), ipAddress, userAgent, roleNames);
        java.util.Set<String> permissions = userRoleService.getPermissionsByUserId(user.getUserId());
        String token = jwtUtil.generateToken(user, session.getSessionId(), roleNames, permissions,
                session.getMaxExpiresAt());
        activityRecordingService.recordLogin(user.getUserId(), ipAddress, userAgent, true);
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
            String userId = jwtUtil.extractUserId(token);
            userSessionService.revokeSessionBySessionId(sessionId);
            activityRecordingService.record(userId, ActivityType.LOGOUT);
            log.info("User logged out, session terminated: {}", sessionId);
        }
    }

    @Override
    public String extractUserIdFromToken(String token) {
        return jwtUtil.extractUserId(token);
    }
}
