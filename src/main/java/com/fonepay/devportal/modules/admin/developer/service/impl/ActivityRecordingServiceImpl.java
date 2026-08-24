package com.fonepay.devportal.modules.admin.developer.service.impl;

import java.time.Clock;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.fonepay.devportal.common.constant.PaginationConstants;
import com.fonepay.devportal.common.constant.enums.ActivityType;
import com.fonepay.devportal.common.dto.PageResponse;
import com.fonepay.devportal.common.exception.BadRequestException;
import com.fonepay.devportal.common.exception.ResourceNotFoundException;
import com.fonepay.devportal.common.util.IdGenerator;
import com.fonepay.devportal.modules.admin.developer.document.Activity;
import com.fonepay.devportal.modules.admin.developer.dto.response.ActivityResponse;
import com.fonepay.devportal.modules.admin.developer.dto.response.LoginHistoryResponse;
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
        activityRepository.save(Activity.builder()
                .id(IdGenerator.nextUlid())
                .userId(userId)
                .type(type)
                .occurredAt(clock.instant())
                .build());
    }

    @Override
    public void recordLogin(String userId, String ipAddress, String userAgent, boolean success) {
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

        Page<Activity> result = (type == null)
                ? activityRepository.findByUserId(userId, pageable)
                : activityRepository.findByUserIdAndType(userId, type, pageable);

        return PageResponse.of(result, result.getContent().stream().map(this::toActivity).toList());
    }

    @Override
    public PageResponse<LoginHistoryResponse> getLoginHistory(
            String userId, int page, int size, String sortDirection) {

        ensureUserExists(userId);
        Page<Activity> result = activityRepository.findByUserIdAndType(
                userId, ActivityType.LOGIN, pageRequest(page, size, sortDirection, "occurredAt"));

        return PageResponse.of(result, result.getContent().stream().map(this::toLoginHistory).toList());
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
        if (size < 1 || size > PaginationConstants.MAX_PAGE_SIZE) {
            throw new BadRequestException("size must be between 1 and " + PaginationConstants.MAX_PAGE_SIZE);
        }
        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(
                    sortDirection == null ? PaginationConstants.DEFAULT_SORT_DIRECTION : sortDirection);
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
