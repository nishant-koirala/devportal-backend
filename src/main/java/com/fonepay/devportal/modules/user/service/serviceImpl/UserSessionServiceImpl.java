package com.fonepay.devportal.modules.user.service.serviceImpl;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.fonepay.devportal.common.constant.enums.SessionStatus;
import com.fonepay.devportal.common.util.IdGenerator;
import com.fonepay.devportal.modules.user.document.UserSession;
import com.fonepay.devportal.modules.user.repository.UserSessionRepository;
import com.fonepay.devportal.modules.user.service.UserSessionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSessionServiceImpl implements UserSessionService {

    private final UserSessionRepository userSessionRepository;
    private final Clock clock;

    @Override
    public UserSession createSession(String userId, String ipAddress, String userAgent, long expirationMs) {
        Instant now = clock.instant();

        // Revoke any existing active session from the same device / IP
        if (ipAddress != null && userAgent != null) {
            List<UserSession> existingSameDeviceSessions = userSessionRepository
                    .findByUserIdAndIpAddressAndUserAgentAndStatus(userId, ipAddress, userAgent, SessionStatus.ACTIVE);
            if (!existingSameDeviceSessions.isEmpty()) {
                existingSameDeviceSessions.forEach(s -> {
                    s.setStatus(SessionStatus.REVOKED);
                    s.setRevokedAt(now);
                });
                userSessionRepository.saveAll(existingSameDeviceSessions);
                log.info("Revoked {} existing active session(s) from same device (IP: {}) for user: {}",
                        existingSameDeviceSessions.size(), ipAddress, userId);
            }
        }

        String sessionId = IdGenerator.nextUlid();
        Instant expiresAt = now.plusMillis(expirationMs);

        UserSession session = UserSession.builder()
                .sessionId(sessionId)
                .userId(userId)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .createdAt(now)
                .lastActivityAt(now)
                .expiresAt(expiresAt)
                .maxExpiresAt(expiresAt)
                .status(SessionStatus.ACTIVE)
                .build();

        UserSession savedSession = userSessionRepository.save(session);
        log.info("Active session created with ID: {} for user: {}", sessionId, userId);
        return savedSession;
    }

    @Override
    public void revokeSessionBySessionId(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }
        userSessionRepository.findBySessionId(sessionId).ifPresent(session -> {
            session.setStatus(SessionStatus.REVOKED);
            session.setRevokedAt(clock.instant());
            userSessionRepository.save(session);
            log.info("User session revoked for sessionId: {}", sessionId);
        });
    }

    @Override
    public void revokeAllActiveSessions(String userId) {
        Instant now = clock.instant();
        List<UserSession> activeSessions = userSessionRepository.findByUserIdAndStatus(userId, SessionStatus.ACTIVE);
        if (!activeSessions.isEmpty()) {
            activeSessions.forEach(session -> {
                session.setStatus(SessionStatus.REVOKED);
                session.setRevokedAt(now);
            });
            userSessionRepository.saveAll(activeSessions);
            log.info("Revoked {} active sessions for user: {}", activeSessions.size(), userId);
        }
    }

    @Override
    public void revokeAllActiveSessionsExcept(String userId, String currentSessionId) {
        Instant now = clock.instant();
        List<UserSession> toRevoke = userSessionRepository.findByUserIdAndStatus(userId, SessionStatus.ACTIVE)
                .stream()
                .filter(session -> currentSessionId == null || !currentSessionId.equals(session.getSessionId()))
                .toList();
        if (!toRevoke.isEmpty()) {
            toRevoke.forEach(session -> {
                session.setStatus(SessionStatus.REVOKED);
                session.setRevokedAt(now);
            });
            userSessionRepository.saveAll(toRevoke);
            log.info("Revoked {} other active session(s) for user: {} (kept {})",
                    toRevoke.size(), userId, currentSessionId);
        }
    }

    @Override
    public Optional<UserSession> getActiveSession(String sessionId) {
        return userSessionRepository.findBySessionId(sessionId)
                .filter(s -> s.getStatus() == SessionStatus.ACTIVE && s.getExpiresAt().isAfter(clock.instant()));
    }
}
