package com.fonepay.devportal.modules.auth.service;

import java.time.Clock;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fonepay.devportal.common.constant.enums.AuthStatus;
import com.fonepay.devportal.common.constant.enums.SessionStatus;
import com.fonepay.devportal.common.constant.enums.UserStatus;
import com.fonepay.devportal.common.exception.UnauthorizedException;
import com.fonepay.devportal.common.util.IdGenerator;
import com.fonepay.devportal.modules.auth.dto.reponse.AuthResponse;
import com.fonepay.devportal.modules.auth.dto.request.LoginRequest;
import com.fonepay.devportal.modules.auth.dto.request.OtpVerifyRequest;
import com.fonepay.devportal.modules.auth.dto.response.OtpResponse;
import com.fonepay.devportal.modules.auth.mapper.AuthMapper;
import com.fonepay.devportal.modules.user.entity.User;
import com.fonepay.devportal.modules.user.entity.UserSession;
import com.fonepay.devportal.modules.user.repository.UserRepository;
import com.fonepay.devportal.modules.user.repository.UserSessionRepository;
import com.fonepay.devportal.security.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{

    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthMapper authMapper;
    private final OtpService otpService;
    private final TempTokenService tempTokenService;
    private final EmailService emailService;
    private final Clock clock;

    @Value("${jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

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

        if (ipAddress != null && userAgent != null) {
            java.util.List<UserSession> existingSameDeviceSessions = userSessionRepository
                    .findByUserIdAndIpAddressAndUserAgentAndStatus(user.getUserId(), ipAddress, userAgent, SessionStatus.ACTIVE);
            if (!existingSameDeviceSessions.isEmpty()) {
                existingSameDeviceSessions.forEach(s -> {
                    s.setStatus(SessionStatus.REVOKED);
                    s.setRevokedAt(now);
                });
                userSessionRepository.saveAll(existingSameDeviceSessions);
                log.info("Revoked {} existing active session(s) from same device (IP: {}) for user: {}",
                        existingSameDeviceSessions.size(), ipAddress, user.getUserId());
            }
        }

        String sessionId = IdGenerator.nextUlid();
        Instant expiresAt = now.plusMillis(jwtExpirationMs);

        UserSession session = UserSession.builder()
                .sessionId(sessionId)
                .userId(user.getUserId())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .createdAt(now)
                .lastActivityAt(now)
                .expiresAt(expiresAt)
                .status(SessionStatus.ACTIVE)
                .build();

        userSessionRepository.save(session);
        log.info("Active session created with ID: {} for user: {}", sessionId, user.getUserId());

        user.setLastLoginAt(now);
        userRepository.save(user);

        // Check if user requires OTP (ADMIN and EDITOR roles)
        if (user.requiresOtp()) {
            // Generate OTP
            String otpCode = otpService.generateOtp(user);
            userRepository.save(user);

            // Send OTP via email
            emailService.sendOtpEmail(user.getEmail(), otpCode, user.getFullName());

            // Generate temporary token for OTP verification flow
            String tempToken = tempTokenService.generateTempToken(user.getUserId(), sessionId);
            return authMapper.toAuthResponse(user, tempToken, "OTP sent to your email", AuthStatus.OTP_REQUIRED);
        }

        // No OTP required (DEVELOPER role)
        String token = jwtUtil.generateToken(user, sessionId);
        return authMapper.toAuthResponse(user, token, "Login successful", AuthStatus.LOGIN_SUCCESS);
    }

    @Override
    public void logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }

        String token = authHeader.substring(7);
        String sessionId = jwtUtil.extractSessionId(token);

        if (sessionId != null) {
            userSessionRepository.findBySessionId(sessionId).ifPresent(session -> {
                session.setStatus(SessionStatus.REVOKED);
                session.setRevokedAt(clock.instant());
                userSessionRepository.save(session);
                log.info("User session revoked for sessionId: {}", sessionId);
            });
        }
    }

    @Override
    public OtpResponse requestOtp(String tempToken) {
        if (!tempTokenService.validateTempToken(tempToken)) {
            throw new UnauthorizedException("Invalid or expired temporary token");
        }

        String userId = tempTokenService.extractUserId(tempToken);
        String sessionId = tempTokenService.extractSessionId(tempToken);

        if (userId == null || sessionId == null) {
            throw new UnauthorizedException("Invalid temporary token");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (!user.requiresOtp()) {
            throw new UnauthorizedException("OTP not required for this user role");
        }

        // Check if there's already a pending OTP
        if (otpService.hasPendingOtp(user)) {
            long remainingSeconds = otpService.getOtpRemainingSeconds(user);
            return authMapper.toOtpResponse(
                    "OTP already sent. Please check your email or wait for it to expire.",
                    (int) remainingSeconds);
        }

        // Generate new OTP
        String otpCode = otpService.generateOtp(user);
        userRepository.save(user);

        // Send OTP via email
        emailService.sendOtpEmail(user.getEmail(), otpCode, user.getFullName());

        log.info("OTP resent for user: {}", userId);

        return authMapper.toOtpResponse(
                "OTP sent to your email",
                (int) otpService.getOtpRemainingSeconds(user));
    }

    @Override
    public AuthResponse verifyOtp(String tempToken, OtpVerifyRequest request) {
        if (!tempTokenService.validateTempToken(tempToken)) {
            throw new UnauthorizedException("Invalid or expired temporary token");
        }

        String userId = tempTokenService.extractUserId(tempToken);
        String sessionId = tempTokenService.extractSessionId(tempToken);

        if (userId == null || sessionId == null) {
            throw new UnauthorizedException("Invalid temporary token");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (!user.requiresOtp()) {
            throw new UnauthorizedException("OTP verification not required for this user");
        }

        // Verify the OTP code
        if (!otpService.verifyOtp(user, request.getCode())) {
            userRepository.save(user);

            if (user.getOtpStatus() == com.fonepay.devportal.common.constant.enums.OtpStatus.EXPIRED) {
                throw new UnauthorizedException("OTP has expired. Please request a new one.");
            } else if (user.getOtpStatus() == com.fonepay.devportal.common.constant.enums.OtpStatus.FAILED) {
                throw new UnauthorizedException("Max OTP attempts exceeded. Please request a new OTP.");
            } else {
                int remainingAttempts = 3 - user.getOtpAttempts(); // maxAttempts is 3
                throw new UnauthorizedException("Invalid OTP code. " + remainingAttempts + " attempts remaining.");
            }
        }

        // OTP verified successfully
        userRepository.save(user);
        log.info("OTP verification successful for user: {}", userId);

        // Clear OTP data
        otpService.clearOtp(user);
        userRepository.save(user);

        // Generate full JWT token
        String token = jwtUtil.generateToken(user, sessionId);

        return authMapper.toAuthResponse(user, token, "Login successful", AuthStatus.LOGIN_SUCCESS);
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
}