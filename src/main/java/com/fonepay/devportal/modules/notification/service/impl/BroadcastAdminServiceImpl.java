package com.fonepay.devportal.modules.notification.service.impl;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.fonepay.devportal.common.dto.PageResponse;
import com.fonepay.devportal.common.exception.BadRequestException;
import com.fonepay.devportal.common.exception.ResourceNotFoundException;
import com.fonepay.devportal.common.util.IdGenerator;
import com.fonepay.devportal.modules.notification.document.Broadcast;
import com.fonepay.devportal.modules.notification.dto.request.BroadcastFilterRequest;
import com.fonepay.devportal.modules.notification.dto.request.CreateBroadcastRequest;
import com.fonepay.devportal.modules.notification.dto.request.UpdateBroadcastRequest;
import com.fonepay.devportal.modules.notification.dto.response.BroadcastMetricsResponse;
import com.fonepay.devportal.modules.notification.dto.response.BroadcastResponse;
import com.fonepay.devportal.modules.notification.enums.BroadcastStatus;
import com.fonepay.devportal.modules.notification.event.BroadcastCancelledEvent;
import com.fonepay.devportal.modules.notification.event.BroadcastCreatedEvent;
import com.fonepay.devportal.modules.notification.event.BroadcastUpdatedEvent;
import com.fonepay.devportal.modules.notification.mapper.BroadcastMapper;
import com.fonepay.devportal.modules.notification.repository.BroadcastRepository;
import com.fonepay.devportal.modules.notification.repository.UserBroadcastInteractionRepository;
import com.fonepay.devportal.modules.notification.service.BroadcastAdminService;
import com.fonepay.devportal.modules.notification.specification.BroadcastQueryBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BroadcastAdminServiceImpl implements BroadcastAdminService {

    private final BroadcastRepository broadcastRepository;
    private final UserBroadcastInteractionRepository interactionRepository;
    private final BroadcastMapper broadcastMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    @Override
    public BroadcastResponse createBroadcast(CreateBroadcastRequest request, String adminId) {
        Instant now = clock.instant();

        Instant startsAt = request.getStartsAt() != null ? request.getStartsAt() : now;
        Instant expiresAt = request.getExpiresAt();

        if (expiresAt != null && expiresAt.isBefore(startsAt)) {
            throw new BadRequestException("Broadcast expiration time cannot be before start time");
        }

        Broadcast broadcast = Broadcast.builder()
                .id(IdGenerator.nextUlid())
                .title(request.getTitle().trim())
                .message(request.getMessage().trim())
                .targetRole(request.getTargetRole())
                .displayModes(request.getDisplayModes())
                .priority(request.getPriority())
                .category(request.getCategory())
                .isDismissible(request.getIsDismissible() != null ? request.getIsDismissible() : true)
                .actionUrl(request.getActionUrl())
                .actionLabel(request.getActionLabel())
                .startsAt(startsAt)
                .expiresAt(expiresAt)
                .status(BroadcastStatus.ACTIVE)
                .createdBy(adminId)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Broadcast saved = broadcastRepository.save(broadcast);
        log.info("Admin [{}] created broadcast [{}] for target role [{}]", adminId, saved.getId(), saved.getTargetRole());

        eventPublisher.publishEvent(new BroadcastCreatedEvent(saved));

        return broadcastMapper.toResponse(saved);
    }

    @Override
    public BroadcastResponse updateBroadcast(String broadcastId, UpdateBroadcastRequest request, String adminId) {
        Broadcast broadcast = findBroadcastOrThrow(broadcastId);
        Instant now = clock.instant();

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            broadcast.setTitle(request.getTitle().trim());
        }
        if (request.getMessage() != null && !request.getMessage().isBlank()) {
            broadcast.setMessage(request.getMessage().trim());
        }
        if (request.getTargetRole() != null) {
            broadcast.setTargetRole(request.getTargetRole());
        }
        if (request.getDisplayModes() != null && !request.getDisplayModes().isEmpty()) {
            broadcast.setDisplayModes(request.getDisplayModes());
        }
        if (request.getPriority() != null) {
            broadcast.setPriority(request.getPriority());
        }
        if (request.getCategory() != null) {
            broadcast.setCategory(request.getCategory());
        }
        if (request.getIsDismissible() != null) {
            broadcast.setDismissible(request.getIsDismissible());
        }
        if (request.getActionUrl() != null) {
            broadcast.setActionUrl(request.getActionUrl());
        }
        if (request.getActionLabel() != null) {
            broadcast.setActionLabel(request.getActionLabel());
        }
        if (request.getStartsAt() != null) {
            broadcast.setStartsAt(request.getStartsAt());
        }
        if (request.getExpiresAt() != null) {
            if (broadcast.getStartsAt() != null && request.getExpiresAt().isBefore(broadcast.getStartsAt())) {
                throw new BadRequestException("Broadcast expiration time cannot be before start time");
            }
            broadcast.setExpiresAt(request.getExpiresAt());
        }
        if (request.getStatus() != null) {
            broadcast.setStatus(request.getStatus());
        }

        broadcast.setUpdatedAt(now);
        Broadcast updated = broadcastRepository.save(broadcast);
        log.info("Admin [{}] updated broadcast [{}]", adminId, broadcastId);

        eventPublisher.publishEvent(new BroadcastUpdatedEvent(updated));

        return broadcastMapper.toResponse(updated);
    }

    @Override
    public BroadcastResponse cancelBroadcast(String broadcastId, String adminId) {
        Broadcast broadcast = findBroadcastOrThrow(broadcastId);
        broadcast.setStatus(BroadcastStatus.CANCELLED);
        broadcast.setUpdatedAt(clock.instant());

        Broadcast updated = broadcastRepository.save(broadcast);
        log.info("Admin [{}] cancelled broadcast [{}]", adminId, broadcastId);

        eventPublisher.publishEvent(new BroadcastCancelledEvent(broadcastId));

        return broadcastMapper.toResponse(updated);
    }

    @Override
    public BroadcastResponse getBroadcastById(String broadcastId) {
        Broadcast broadcast = findBroadcastOrThrow(broadcastId);
        return broadcastMapper.toResponse(broadcast);
    }

    @Override
    public PageResponse<BroadcastResponse> getBroadcasts(BroadcastFilterRequest filter) {
        Sort.Direction direction = "ASC".equalsIgnoreCase(filter.getSortDirection())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        String sortBy = filter.getSortBy() != null && !filter.getSortBy().isBlank()
                ? filter.getSortBy()
                : "createdAt";
        if (!BroadcastQueryBuilder.allowedSortFields().contains(sortBy)) {
            sortBy = "createdAt";
        }

        int page = Math.max(0, filter.getPage());
        int size = filter.getSize() > 0 ? filter.getSize() : 20;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<Broadcast> result = broadcastRepository.findAll(BroadcastQueryBuilder.fromFilter(filter), pageable);
        List<BroadcastResponse> mapped = result.getContent().stream()
                .map(broadcastMapper::toResponse)
                .toList();

        return PageResponse.of(result, mapped);
    }

    @Override
    public BroadcastMetricsResponse getBroadcastMetrics(String broadcastId) {
        findBroadcastOrThrow(broadcastId);

        long readCount = interactionRepository.countByBroadcastIdAndIsReadTrue(broadcastId);
        long dismissedCount = interactionRepository.countByBroadcastIdAndIsDismissedTrue(broadcastId);

        return BroadcastMetricsResponse.builder()
                .broadcastId(broadcastId)
                .totalReadCount(readCount)
                .totalDismissedCount(dismissedCount)
                .build();
    }

    private Broadcast findBroadcastOrThrow(String broadcastId) {
        if (broadcastId == null || broadcastId.isBlank()) {
            throw new BadRequestException("Broadcast ID must not be blank");
        }
        return broadcastRepository.findById(broadcastId)
                .orElseThrow(() -> new ResourceNotFoundException("Broadcast not found with ID: " + broadcastId));
    }
}
