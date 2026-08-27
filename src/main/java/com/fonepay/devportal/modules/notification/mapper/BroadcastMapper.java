package com.fonepay.devportal.modules.notification.mapper;

import org.springframework.stereotype.Component;

import com.fonepay.devportal.modules.notification.document.Broadcast;
import com.fonepay.devportal.modules.notification.document.UserBroadcastInteraction;
import com.fonepay.devportal.modules.notification.dto.response.BroadcastResponse;
import com.fonepay.devportal.modules.notification.dto.response.StaffBroadcastResponse;

@Component
public class BroadcastMapper {

    public BroadcastResponse toResponse(Broadcast broadcast) {
        if (broadcast == null) {
            return null;
        }

        return BroadcastResponse.builder()
                .id(broadcast.getId())
                .title(broadcast.getTitle())
                .message(broadcast.getMessage())
                .targetRole(broadcast.getTargetRole())
                .displayModes(broadcast.getDisplayModes())
                .priority(broadcast.getPriority())
                .category(broadcast.getCategory())
                .isDismissible(broadcast.isDismissible())
                .actionUrl(broadcast.getActionUrl())
                .actionLabel(broadcast.getActionLabel())
                .startsAt(broadcast.getStartsAt())
                .expiresAt(broadcast.getExpiresAt())
                .status(broadcast.getStatus())
                .createdBy(broadcast.getCreatedBy())
                .createdAt(broadcast.getCreatedAt())
                .updatedAt(broadcast.getUpdatedAt())
                .build();
    }

    public StaffBroadcastResponse toStaffResponse(Broadcast broadcast, UserBroadcastInteraction interaction) {
        if (broadcast == null) {
            return null;
        }

        boolean isRead = interaction != null && interaction.isRead();
        boolean isDismissed = interaction != null && interaction.isDismissed();

        return StaffBroadcastResponse.builder()
                .id(broadcast.getId())
                .title(broadcast.getTitle())
                .message(broadcast.getMessage())
                .targetRole(broadcast.getTargetRole())
                .displayModes(broadcast.getDisplayModes())
                .priority(broadcast.getPriority())
                .category(broadcast.getCategory())
                .isDismissible(broadcast.isDismissible())
                .actionUrl(broadcast.getActionUrl())
                .actionLabel(broadcast.getActionLabel())
                .startsAt(broadcast.getStartsAt())
                .expiresAt(broadcast.getExpiresAt())
                .isRead(isRead)
                .readAt(interaction != null ? interaction.getReadAt() : null)
                .isDismissed(isDismissed)
                .dismissedAt(interaction != null ? interaction.getDismissedAt() : null)
                .createdAt(broadcast.getCreatedAt())
                .build();
    }
}
