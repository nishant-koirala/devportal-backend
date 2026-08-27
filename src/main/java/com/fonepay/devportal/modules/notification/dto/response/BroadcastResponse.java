package com.fonepay.devportal.modules.notification.dto.response;

import java.time.Instant;
import java.util.Set;

import com.fonepay.devportal.modules.notification.enums.BroadcastCategory;
import com.fonepay.devportal.modules.notification.enums.BroadcastDisplayMode;
import com.fonepay.devportal.modules.notification.enums.BroadcastPriority;
import com.fonepay.devportal.modules.notification.enums.BroadcastStatus;
import com.fonepay.devportal.modules.notification.enums.BroadcastTargetRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BroadcastResponse {

    private String id;
    private String title;
    private String message;
    private BroadcastTargetRole targetRole;
    private Set<BroadcastDisplayMode> displayModes;
    private BroadcastPriority priority;
    private BroadcastCategory category;
    private boolean isDismissible;
    private String actionUrl;
    private String actionLabel;
    private Instant startsAt;
    private Instant expiresAt;
    private BroadcastStatus status;
    private String createdBy;
    private Instant createdAt;
    private Instant updatedAt;
}
