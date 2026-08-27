package com.fonepay.devportal.modules.notification.dto.request;

import java.time.Instant;
import java.util.Set;

import com.fonepay.devportal.modules.notification.enums.BroadcastCategory;
import com.fonepay.devportal.modules.notification.enums.BroadcastDisplayMode;
import com.fonepay.devportal.modules.notification.enums.BroadcastPriority;
import com.fonepay.devportal.modules.notification.enums.BroadcastTargetRole;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBroadcastRequest {

    @NotBlank(message = "Broadcast title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotBlank(message = "Broadcast message is required")
    @Size(max = 5000, message = "Message must not exceed 5000 characters")
    private String message;

    @NotNull(message = "Target role is required")
    private BroadcastTargetRole targetRole;

    @NotEmpty(message = "At least one display mode must be selected")
    private Set<BroadcastDisplayMode> displayModes;

    @Builder.Default
    private BroadcastPriority priority = BroadcastPriority.NORMAL;

    @Builder.Default
    private BroadcastCategory category = BroadcastCategory.GENERAL;

    @Builder.Default
    private Boolean isDismissible = true;

    private String actionUrl;

    private String actionLabel;

    private Instant startsAt;

    private Instant expiresAt;
}
