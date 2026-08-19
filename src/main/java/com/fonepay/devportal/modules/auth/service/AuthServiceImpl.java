package com.fonepay.devportal.modules.auth.service;

import java.time.Clock;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fonepay.devportal.common.constant.enums.SessionStatus;
import com.fonepay.devportal.common.constant.enums.UserStatus;
import com.fonepay.devportal.common.exception.UnauthorizedException;
import com.fonepay.devportal.common.util.IdGenerator;
import com.fonepay.devportal.modules.auth.dto.reponse.AuthResponse;
import com.fonepay.devportal.modules.auth.dto.request.LoginRequest;
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
    private final Clock clock;

    @Value("${jwt.expiration-ms}")
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

        String token = jwtUtil.generateToken(user, sessionId);

        return authMapper.toAuthResponse(user, token, "Login successful");
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
}
