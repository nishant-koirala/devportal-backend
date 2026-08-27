package com.fonepay.devportal.modules.notification.dto.request;

import java.time.Instant;
import java.util.Set;

import com.fonepay.devportal.modules.notification.enums.BroadcastCategory;
import com.fonepay.devportal.modules.notification.enums.BroadcastDisplayMode;
import com.fonepay.devportal.modules.notification.enums.BroadcastPriority;
import com.fonepay.devportal.modules.notification.enums.BroadcastStatus;
import com.fonepay.devportal.modules.notification.enums.BroadcastTargetRole;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBroadcastRequest {

    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Size(max = 5000, message = "Message must not exceed 5000 characters")
    private String message;

    private BroadcastTargetRole targetRole;

    private Set<BroadcastDisplayMode> displayModes;

    private BroadcastPriority priority;

    private BroadcastCategory category;

    private Boolean isDismissible;

    private String actionUrl;

    private String actionLabel;

    private Instant startsAt;

    private Instant expiresAt;

    private BroadcastStatus status;
}
