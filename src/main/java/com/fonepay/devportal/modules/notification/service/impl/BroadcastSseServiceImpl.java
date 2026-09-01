package com.fonepay.devportal.modules.notification.service.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fonepay.devportal.common.util.IdGenerator;
import com.fonepay.devportal.modules.notification.document.Broadcast;
import com.fonepay.devportal.modules.notification.dto.response.BroadcastResponse;
import com.fonepay.devportal.modules.notification.enums.BroadcastTargetRole;
import com.fonepay.devportal.modules.notification.mapper.BroadcastMapper;
import com.fonepay.devportal.modules.notification.service.BroadcastSseService;
import com.fonepay.devportal.modules.user.document.AssignedRole;
import com.fonepay.devportal.modules.user.document.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BroadcastSseServiceImpl implements BroadcastSseService {

    private static final long SSE_TIMEOUT_MS = 30 * 60 * 1000L; // 30 minutes

    private final BroadcastMapper broadcastMapper;

    // userId -> Map<emitterId, SseEmitter>
    private final Map<String, Map<String, SseEmitter>> userEmittersMap = new ConcurrentHashMap<>();

    // userId -> Set<roleName>
    private final Map<String, Set<String>> userRolesMap = new ConcurrentHashMap<>();

    @Override
    public SseEmitter subscribe(User user) {
        String userId = user != null && user.getUserId() != null ? user.getUserId() : "anonymous";
        String emitterId = IdGenerator.nextUlid();

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);

        userEmittersMap.computeIfAbsent(userId, k -> new ConcurrentHashMap<>()).put(emitterId, emitter);

        Set<String> roles = extractRoles(user);
        userRolesMap.put(userId, roles);

        emitter.onCompletion(() -> removeEmitter(userId, emitterId, "Completed"));
        emitter.onTimeout(() -> removeEmitter(userId, emitterId, "Timed out"));
        emitter.onError(e -> removeEmitter(userId, emitterId, "Error: " + e.getMessage()));

        try {
            emitter.send(SseEmitter.event()
                    .name("CONNECT")
                    .id(emitterId)
                    .data(Map.of(
                            "status", "CONNECTED",
                            "userId", userId,
                            "connectionId", emitterId)));
            log.info("Staff user [{}] subscribed to SSE stream with connectionId [{}]", userId, emitterId);
        } catch (IOException e) {
            log.warn("Failed to send initial SSE handshake to user [{}]: {}", userId, e.getMessage());
            removeEmitter(userId, emitterId, "Initial handshake failed");
        }

        return emitter;
    }

    @Override
    public void sendBroadcast(Broadcast broadcast, String eventType) {
        if (broadcast == null) {
            return;
        }

        BroadcastResponse response = broadcastMapper.toResponse(broadcast);
        BroadcastTargetRole targetRole = broadcast.getTargetRole();

        userEmittersMap.forEach((userId, emitters) -> {
            Set<String> roles = userRolesMap.getOrDefault(userId, Collections.emptySet());

            if (isEligible(roles, targetRole)) {
                emitters.forEach((emitterId, emitter) -> {
                    try {
                        emitter.send(SseEmitter.event()
                                .name(eventType)
                                .id(broadcast.getId())
                                .data(response));
                    } catch (IOException e) {
                        log.warn("Error sending SSE [{}] to user [{}] connection [{}]: {}", eventType, userId, emitterId, e.getMessage());
                        removeEmitter(userId, emitterId, "Socket write failure");
                    }
                });
            }
        });
    }

    @Override
    public void sendCancellation(String broadcastId) {
        if (broadcastId == null) {
            return;
        }

        Map<String, String> payload = Map.of("broadcastId", broadcastId);

        userEmittersMap.forEach((userId, emitters) -> {
            emitters.forEach((emitterId, emitter) -> {
                try {
                    emitter.send(SseEmitter.event()
                            .name("BROADCAST_CANCELLED")
                            .id(broadcastId)
                            .data(payload));
                } catch (IOException e) {
                    log.warn("Error sending cancellation SSE to user [{}] connection [{}]: {}", userId, emitterId, e.getMessage());
                    removeEmitter(userId, emitterId, "Socket write failure");
                }
            });
        });
    }

    @Override
    @Scheduled(fixedRate = 25000)
    public void sendHeartbeat() {
        if (userEmittersMap.isEmpty()) {
            return;
        }

        userEmittersMap.forEach((userId, emitters) -> {
            emitters.forEach((emitterId, emitter) -> {
                try {
                    emitter.send(SseEmitter.event()
                            .name("HEARTBEAT")
                            .data(Map.of("type", "HEARTBEAT", "timestamp", System.currentTimeMillis())));
                } catch (IOException e) {
                    log.debug("Heartbeat failed for user [{}] connection [{}], cleaning up", userId, emitterId);
                    removeEmitter(userId, emitterId, "Heartbeat failed");
                }
            });
        });
    }

    @Override
    public int getActiveConnectionCount() {
        return userEmittersMap.values().stream().mapToInt(Map::size).sum();
    }

    private void removeEmitter(String userId, String emitterId, String reason) {
        Map<String, SseEmitter> emitters = userEmittersMap.get(userId);
        if (emitters != null) {
            emitters.remove(emitterId);
            if (emitters.isEmpty()) {
                userEmittersMap.remove(userId);
                userRolesMap.remove(userId);
            }
        }
        log.debug("Removed SSE connection [{}] for user [{}]. Reason: {}", emitterId, userId, reason);
    }

    private boolean isEligible(Set<String> roles, BroadcastTargetRole targetRole) {
        if (targetRole == null || targetRole == BroadcastTargetRole.ALL_STAFF) {
            return true;
        }

        boolean isAdmin = roles.stream().anyMatch(r -> "ADMIN".equalsIgnoreCase(r));
        boolean isEditor = roles.stream().anyMatch(r -> "EDITOR".equalsIgnoreCase(r));

        if (targetRole == BroadcastTargetRole.ADMINS_ONLY) {
            return isAdmin;
        }

        if (targetRole == BroadcastTargetRole.CMS_EDITORS) {
            return isAdmin || isEditor;
        }

        return true;
    }

    private Set<String> extractRoles(User user) {
        if (user == null || user.getRoles() == null) {
            return Collections.emptySet();
        }
        return user.getRoles().stream()
                .map(AssignedRole::getRoleName)
                .collect(Collectors.toSet());
    }
}
