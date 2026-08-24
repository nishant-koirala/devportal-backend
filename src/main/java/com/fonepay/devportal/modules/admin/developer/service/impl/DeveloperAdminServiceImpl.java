package com.fonepay.devportal.modules.admin.developer.service.impl;

import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fonepay.devportal.common.constant.enums.UserStatus;
import com.fonepay.devportal.common.exception.BadRequestException;
import com.fonepay.devportal.common.exception.ResourceNotFoundException;
import com.fonepay.devportal.modules.admin.developer.dto.request.UpdateDeveloperStatusRequest;
import com.fonepay.devportal.modules.admin.developer.dto.response.DeveloperDetailResponse;
import com.fonepay.devportal.modules.admin.developer.service.DeveloperAdminService;
import com.fonepay.devportal.modules.user.document.AssignedRole;
import com.fonepay.devportal.modules.user.document.User;
import com.fonepay.devportal.modules.user.repository.UserRepository;
import com.fonepay.devportal.modules.user.service.UserSessionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeveloperAdminServiceImpl implements DeveloperAdminService {

    private final UserRepository userRepository;
    private final UserSessionService userSessionService;
    private final Clock clock;

    @Override
    public DeveloperDetailResponse getDeveloperById(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new BadRequestException("Developer ID must not be blank");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Developer not found with ID: " + userId));

        return toDeveloperDetailResponse(user);
    }

    @Override
    public DeveloperDetailResponse updateDeveloperStatus(String userId, UpdateDeveloperStatusRequest request) {
        if (userId == null || userId.isBlank()) {
            throw new BadRequestException("Developer ID must not be blank");
        }

        UserStatus targetStatus = request.getStatus();
        if (targetStatus != UserStatus.ACTIVE && targetStatus != UserStatus.INACTIVE && targetStatus != UserStatus.DEACTIVATED) {
            throw new BadRequestException("Invalid status transition: Only ACTIVE, INACTIVE, or DEACTIVATED are supported");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Developer not found with ID: " + userId));

        UserStatus currentStatus = user.getStatus();
        if (currentStatus == targetStatus) {
            throw new BadRequestException("Developer account is already " + targetStatus);
        }

        Instant now = clock.instant();
        user.setStatus(targetStatus);
        user.setUpdatedAt(now);

        if (targetStatus == UserStatus.INACTIVE || targetStatus == UserStatus.DEACTIVATED) {
            user.setDeactivatedAt(now);
            // Invalidate active sessions immediately upon deactivation
            userSessionService.revokeAllActiveSessions(userId);
            log.info("Revoked active sessions for deactivated developer: {}", userId);
        } else if (targetStatus == UserStatus.ACTIVE) {
            user.setDeactivatedAt(null);
        }

        User savedUser = userRepository.save(user);
        log.info("Admin updated developer status for user {}: {} -> {}", userId, currentStatus, targetStatus);

        return toDeveloperDetailResponse(savedUser);
    }

    private DeveloperDetailResponse toDeveloperDetailResponse(User user) {
        List<String> roleNames = user.getRoles() != null
                ? user.getRoles().stream().map(AssignedRole::getRoleName).collect(Collectors.toList())
                : Collections.emptyList();

        return DeveloperDetailResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .companyName(user.getCompanyName())
                .status(user.getStatus())
                .emailVerified(user.isEmailVerified())
                .lastLoginAt(user.getLastLoginAt())
                .departmentId(user.getDepartmentId())
                .deactivatedAt(user.getDeactivatedAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .roles(roleNames)
                .build();
    }
}
