package com.fonepay.devportal.modules.auth.service.serviceImpl;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fonepay.devportal.common.constant.enums.AuthStatus;
import com.fonepay.devportal.common.constant.enums.UserStatus;
import com.fonepay.devportal.common.exception.UnauthorizedException;
import com.fonepay.devportal.modules.auth.dto.request.LoginRequest;
import com.fonepay.devportal.modules.auth.dto.response.AuthResponse;
import com.fonepay.devportal.modules.auth.mapper.AuthMapper;
import com.fonepay.devportal.modules.auth.policy.MfaPolicy;
import com.fonepay.devportal.modules.auth.service.OtpService;
import com.fonepay.devportal.modules.auth.service.TempTokenService;
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
public class LoginServiceImpl {

    private final UserRepository userRepository;
    private final UserRoleService userRoleService;
    private final UserSessionService userSessionService;
    private final MfaPolicy mfaPolicy;
    private final OtpService otpService;
    private final TempTokenService tempTokenService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthMapper authMapper;
    private final Clock clock;

    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;

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
        UserSession session = userSessionService.createSession(user.getUserId(), ipAddress, userAgent, jwtExpirationMs);

        user.setLastLoginAt(now);
        userRepository.save(user);

        List<String> roleNames = userRoleService.getRoleNamesByUserId(user.getUserId());
        boolean requiresMfa = mfaPolicy.isMfaRequired(user, roleNames);

        if (requiresMfa) {
            String otpCode = otpService.generateOtp(user);
            userRepository.save(user);

            emailService.sendOtpEmail(user.getEmail(), otpCode, user.getFullName());

            String tempToken = tempTokenService.generateTempToken(user.getUserId(), session.getSessionId());
            return authMapper.toAuthResponse(user, tempToken, AuthStatus.OTP_REQUIRED);
        }

        String token = jwtUtil.generateToken(user, session.getSessionId(), roleNames);
        return authMapper.toAuthResponse(user, token, roleNames, AuthStatus.LOGIN_SUCCESS);
    }

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

    public String extractUserIdFromToken(String token) {
        try {
            return jwtUtil.extractUserId(token);
        } catch (Exception e) {
            log.error("Failed to extract user ID from token", e);
            return null;
        }
    }
}
