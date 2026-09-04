package com.fonepay.devportal.modules.user.service.serviceImpl;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
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

    @Value("${session.idle.ttl}")
    private Duration idleTtl;

    @Value("${session.max.ttl}")
    private Duration maxTtl;

    @Value("${session.internal.idle.ttl}")
    private Duration internalIdleTtl;

    @Value("${session.internal.max.ttl}")
    private Duration internalMaxTtl;

    @Override
    public UserSession createSession(String userId, String ipAddress, String userAgent, Collection<String> roleNames) {
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

        boolean internal = isInternal(roleNames);
        Duration idle = internal ? internalIdleTtl : idleTtl;
        Duration max = internal ? internalMaxTtl : maxTtl;
        Instant maxExpiresAt = now.plus(max);
        Instant expiresAt = now.plus(idle);
        if (expiresAt.isAfter(maxExpiresAt)) {
            expiresAt = maxExpiresAt;
        }

        String sessionId = IdGenerator.nextUlid();
        UserSession session = UserSession.builder()
                .sessionId(sessionId)
                .userId(userId)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .createdAt(now)
                .lastActivityAt(now)
                .expiresAt(expiresAt)
                .maxExpiresAt(maxExpiresAt)
                .status(SessionStatus.ACTIVE)
                .build();

        UserSession savedSession = userSessionRepository.save(session);
        log.info("Active {} session created with ID: {} for user: {} (idle={}, max={})",
                internal ? "internal" : "developer", sessionId, userId, idle, max);
        return savedSession;
    }

    private boolean isInternal(Collection<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            return false;
        }
        return roleNames.stream()
                .anyMatch(role -> "ADMIN".equalsIgnoreCase(role) || "EDITOR".equalsIgnoreCase(role));
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
        Instant now = clock.instant();
        return userSessionRepository.findBySessionId(sessionId)
                .filter(s -> s.getStatus() == SessionStatus.ACTIVE)
                .filter(s -> s.getExpiresAt() == null || s.getExpiresAt().isAfter(now))
                .filter(s -> s.getMaxExpiresAt() == null || s.getMaxExpiresAt().isAfter(now));
    }
}
