package com.fonepay.devportal.modules.notification.service.impl;

import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fonepay.devportal.common.exception.BadRequestException;
import com.fonepay.devportal.common.exception.ResourceNotFoundException;
import com.fonepay.devportal.common.util.IdGenerator;
import com.fonepay.devportal.modules.notification.document.Broadcast;
import com.fonepay.devportal.modules.notification.document.UserBroadcastInteraction;
import com.fonepay.devportal.modules.notification.dto.response.StaffBroadcastResponse;
import com.fonepay.devportal.modules.notification.dto.response.StaffBroadcastSummaryResponse;
import com.fonepay.devportal.modules.notification.enums.BroadcastDisplayMode;
import com.fonepay.devportal.modules.notification.enums.BroadcastStatus;
import com.fonepay.devportal.modules.notification.enums.BroadcastTargetRole;
import com.fonepay.devportal.modules.notification.mapper.BroadcastMapper;
import com.fonepay.devportal.modules.notification.repository.BroadcastRepository;
import com.fonepay.devportal.modules.notification.repository.UserBroadcastInteractionRepository;
import com.fonepay.devportal.modules.notification.service.StaffBroadcastService;
import com.fonepay.devportal.modules.user.document.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StaffBroadcastServiceImpl implements StaffBroadcastService {

    private final BroadcastRepository broadcastRepository;
    private final UserBroadcastInteractionRepository interactionRepository;
    private final BroadcastMapper broadcastMapper;
    private final Clock clock;

    @Override
    public List<StaffBroadcastResponse> getActiveBroadcasts(User user, Set<BroadcastDisplayMode> displayModes, boolean excludeDismissed) {
        if (user == null || user.getUserId() == null) {
            return Collections.emptyList();
        }

        Instant now = clock.instant();
        Set<BroadcastTargetRole> targetRoles = resolveTargetRoles(user);

        List<Broadcast> activeBroadcasts = broadcastRepository.findActiveForRoles(BroadcastStatus.ACTIVE, targetRoles, now);

        if (activeBroadcasts.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> broadcastIds = activeBroadcasts.stream().map(Broadcast::getId).toList();
        Map<String, UserBroadcastInteraction> interactionMap = interactionRepository
                .findAllByUserIdAndBroadcastIdIn(user.getUserId(), broadcastIds)
                .stream()
                .collect(Collectors.toMap(UserBroadcastInteraction::getBroadcastId, Function.identity(), (a, b) -> a));

        return activeBroadcasts.stream()
                .filter(b -> displayModes == null || displayModes.isEmpty() || hasMatchingDisplayMode(b, displayModes))
                .filter(b -> {
                    if (!excludeDismissed) {
                        return true;
                    }
                    UserBroadcastInteraction interaction = interactionMap.get(b.getId());
                    return interaction == null || !interaction.isDismissed();
                })
                .map(b -> broadcastMapper.toStaffResponse(b, interactionMap.get(b.getId())))
                .toList();
    }

    @Override
    public StaffBroadcastSummaryResponse getSummary(User user) {
        if (user == null || user.getUserId() == null) {
            return new StaffBroadcastSummaryResponse(0, 0);
        }

        Instant now = clock.instant();
        Set<BroadcastTargetRole> targetRoles = resolveTargetRoles(user);

        List<Broadcast> activeBroadcasts = broadcastRepository.findActiveForRoles(BroadcastStatus.ACTIVE, targetRoles, now);

        if (activeBroadcasts.isEmpty()) {
            return new StaffBroadcastSummaryResponse(0, 0);
        }

        List<String> broadcastIds = activeBroadcasts.stream().map(Broadcast::getId).toList();
        Map<String, UserBroadcastInteraction> interactionMap = interactionRepository
                .findAllByUserIdAndBroadcastIdIn(user.getUserId(), broadcastIds)
                .stream()
                .collect(Collectors.toMap(UserBroadcastInteraction::getBroadcastId, Function.identity(), (a, b) -> a));

        long unreadCount = 0;
        long activeBannerCount = 0;

        for (Broadcast b : activeBroadcasts) {
            UserBroadcastInteraction interaction = interactionMap.get(b.getId());
            boolean isRead = interaction != null && interaction.isRead();
            boolean isDismissed = interaction != null && interaction.isDismissed();

            if (!isRead) {
                unreadCount++;
            }

            if (b.getDisplayModes() != null && b.getDisplayModes().contains(BroadcastDisplayMode.GLOBAL_BANNER) && !isDismissed) {
                activeBannerCount++;
            }
        }

        return StaffBroadcastSummaryResponse.builder()
                .unreadCount(unreadCount)
                .activeBannerCount(activeBannerCount)
                .build();
    }

    @Override
    public StaffBroadcastResponse markAsRead(String broadcastId, String userId) {
        Broadcast broadcast = findBroadcastOrThrow(broadcastId);
        Instant now = clock.instant();

        UserBroadcastInteraction interaction = interactionRepository.findByUserIdAndBroadcastId(userId, broadcastId)
                .orElseGet(() -> UserBroadcastInteraction.builder()
                        .id(IdGenerator.nextMonotonicUlid())
                        .userId(userId)
                        .broadcastId(broadcastId)
                        .createdAt(now)
                        .build());

        interaction.setRead(true);
        interaction.setReadAt(now);
        interaction.setUpdatedAt(now);

        UserBroadcastInteraction saved = interactionRepository.save(interaction);
        log.info("User [{}] marked broadcast [{}] as read", userId, broadcastId);

        return broadcastMapper.toStaffResponse(broadcast, saved);
    }

    @Override
    public void markAllAsRead(User user) {
        if (user == null || user.getUserId() == null) {
            return;
        }

        Instant now = clock.instant();
        Set<BroadcastTargetRole> targetRoles = resolveTargetRoles(user);
        List<Broadcast> activeBroadcasts = broadcastRepository.findActiveForRoles(BroadcastStatus.ACTIVE, targetRoles, now);

        if (activeBroadcasts.isEmpty()) {
            return;
        }

        List<String> broadcastIds = activeBroadcasts.stream().map(Broadcast::getId).toList();
        Map<String, UserBroadcastInteraction> interactionMap = interactionRepository
                .findAllByUserIdAndBroadcastIdIn(user.getUserId(), broadcastIds)
                .stream()
                .collect(Collectors.toMap(UserBroadcastInteraction::getBroadcastId, Function.identity(), (a, b) -> a));

        List<UserBroadcastInteraction> toSave = activeBroadcasts.stream().map(b -> {
            UserBroadcastInteraction interaction = interactionMap.get(b.getId());
            if (interaction == null) {
                interaction = UserBroadcastInteraction.builder()
                        .id(IdGenerator.nextMonotonicUlid())
                        .userId(user.getUserId())
                        .broadcastId(b.getId())
                        .createdAt(now)
                        .build();
            }
            interaction.setRead(true);
            interaction.setReadAt(now);
            interaction.setUpdatedAt(now);
            return interaction;
        }).toList();

        interactionRepository.saveAll(toSave);
        log.info("User [{}] marked all [{}] active broadcasts as read", user.getUserId(), toSave.size());
    }

    @Override
    public StaffBroadcastResponse dismissBroadcast(String broadcastId, String userId) {
        Broadcast broadcast = findBroadcastOrThrow(broadcastId);
        if (!broadcast.isDismissible()) {
            throw new BadRequestException("This broadcast cannot be dismissed as it is mandatory");
        }

        Instant now = clock.instant();
        UserBroadcastInteraction interaction = interactionRepository.findByUserIdAndBroadcastId(userId, broadcastId)
                .orElseGet(() -> UserBroadcastInteraction.builder()
                        .id(IdGenerator.nextMonotonicUlid())
                        .userId(userId)
                        .broadcastId(broadcastId)
                        .createdAt(now)
                        .build());

        interaction.setDismissed(true);
        interaction.setDismissedAt(now);
        interaction.setUpdatedAt(now);

        UserBroadcastInteraction saved = interactionRepository.save(interaction);
        log.info("User [{}] dismissed broadcast [{}]", userId, broadcastId);

        return broadcastMapper.toStaffResponse(broadcast, saved);
    }

    private boolean hasMatchingDisplayMode(Broadcast broadcast, Set<BroadcastDisplayMode> displayModes) {
        if (broadcast.getDisplayModes() == null || broadcast.getDisplayModes().isEmpty()) {
            return false;
        }
        return broadcast.getDisplayModes().stream().anyMatch(displayModes::contains);
    }

    private Set<BroadcastTargetRole> resolveTargetRoles(User user) {
        Set<BroadcastTargetRole> targetRoles = new HashSet<>();
        targetRoles.add(BroadcastTargetRole.ALL_STAFF);

        if (user != null && user.getRoles() != null) {
            boolean isAdmin = user.getRoles().stream().anyMatch(r -> "ADMIN".equalsIgnoreCase(r.getRoleName()));
            boolean isEditor = user.getRoles().stream().anyMatch(r -> "EDITOR".equalsIgnoreCase(r.getRoleName()));

            if (isAdmin) {
                targetRoles.add(BroadcastTargetRole.ADMINS_ONLY);
                targetRoles.add(BroadcastTargetRole.CMS_EDITORS);
            } else if (isEditor) {
                targetRoles.add(BroadcastTargetRole.CMS_EDITORS);
            }
        }
        return targetRoles;
    }

    private Broadcast findBroadcastOrThrow(String broadcastId) {
        if (broadcastId == null || broadcastId.isBlank()) {
            throw new BadRequestException("Broadcast ID must not be blank");
        }
        return broadcastRepository.findById(broadcastId)
                .orElseThrow(() -> new ResourceNotFoundException("Broadcast not found with ID: " + broadcastId));
    }
}
