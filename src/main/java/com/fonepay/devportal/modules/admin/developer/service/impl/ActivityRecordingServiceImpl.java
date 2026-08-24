package com.fonepay.devportal.modules.admin.developer.service.impl;

import java.time.Clock;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.fonepay.devportal.common.constant.enums.ActivityType;
import com.fonepay.devportal.common.exception.BadRequestException;
import com.fonepay.devportal.common.exception.ResourceNotFoundException;
import com.fonepay.devportal.common.util.IdGenerator;
import com.fonepay.devportal.modules.admin.developer.document.Activity;
import com.fonepay.devportal.modules.admin.developer.dto.ActivityResponse;
import com.fonepay.devportal.modules.admin.developer.dto.LoginHistoryResponse;
import com.fonepay.devportal.modules.admin.developer.dto.PageResponse;
import com.fonepay.devportal.modules.admin.developer.repository.ActivityRepository;
import com.fonepay.devportal.modules.admin.developer.service.ActivityRecordingService;
import com.fonepay.devportal.modules.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActivityRecordingServiceImpl implements ActivityRecordingService {

    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    @Override
    public void record(String userId, ActivityType type) {
        // Timeline event only — no IP/success fields.
        activityRepository.save(Activity.builder()
                .id(IdGenerator.nextUlid())
                .userId(userId)
                .type(type)
                .occurredAt(clock.instant())
                .build());
    }

    @Override
    public void recordLogin(String userId, String ipAddress, String userAgent, boolean success) {
        // One LOGIN row is used by both activity and login-history APIs.
        activityRepository.save(Activity.builder()
                .id(IdGenerator.nextUlid())
                .userId(userId)
                .type(ActivityType.LOGIN)
                .occurredAt(clock.instant())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .success(success)
                .build());
    }

    @Override
    public PageResponse<ActivityResponse> getActivity(
            String userId, int page, int size, String sortDirection, ActivityType type) {

        ensureUserExists(userId);
        PageRequest pageable = pageRequest(page, size, sortDirection, "occurredAt");

        // Optional ?type= filter; otherwise return every event for this developer.
        Page<Activity> result = (type == null)
                ? activityRepository.findByUserId(userId, pageable)
                : activityRepository.findByUserIdAndType(userId, type, pageable);

        return PageResponse.<ActivityResponse>builder()
                .content(result.getContent().stream().map(this::toActivity).toList())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .last(result.isLast())
                .build();
    }

    @Override
    public PageResponse<LoginHistoryResponse> getLoginHistory(
            String userId, int page, int size, String sortDirection) {

        ensureUserExists(userId);
        // Login history is LOGIN rows only (success and failure).
        Page<Activity> result = activityRepository.findByUserIdAndType(
                userId, ActivityType.LOGIN, pageRequest(page, size, sortDirection, "occurredAt"));

        return PageResponse.<LoginHistoryResponse>builder()
                .content(result.getContent().stream().map(this::toLoginHistory).toList())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .last(result.isLast())
                .build();
    }

    private void ensureUserExists(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Developer not found");
        }
    }

    private PageRequest pageRequest(int page, int size, String sortDirection, String sortField) {
        if (page < 0) {
            throw new BadRequestException("page must be 0 or greater");
        }
        if (size < 1 || size > 100) {
            throw new BadRequestException("size must be between 1 and 100");
        }
        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(sortDirection == null ? "DESC" : sortDirection);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("sortDirection must be ASC or DESC");
        }
        return PageRequest.of(page, size, Sort.by(direction, sortField));
    }

    private ActivityResponse toActivity(Activity activity) {
        return ActivityResponse.builder()
                .id(activity.getId())
                .userId(activity.getUserId())
                .type(activity.getType())
                .occurredAt(activity.getOccurredAt())
                .build();
    }

    private LoginHistoryResponse toLoginHistory(Activity activity) {
        return LoginHistoryResponse.builder()
                .id(activity.getId())
                .userId(activity.getUserId())
                .loginAt(activity.getOccurredAt())
                .ipAddress(activity.getIpAddress())
                .userAgent(activity.getUserAgent())
                .success(Boolean.TRUE.equals(activity.getSuccess()))
                .build();
    }
}
