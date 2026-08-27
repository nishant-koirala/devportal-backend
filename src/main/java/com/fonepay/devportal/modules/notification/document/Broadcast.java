package com.fonepay.devportal.modules.notification.document;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

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
@Document(collection = "cms_broadcasts")
@CompoundIndexes({
        @CompoundIndex(name = "status_target_starts_idx", def = "{'status': 1, 'target_role': 1, 'starts_at': -1}"),
        @CompoundIndex(name = "status_expires_idx", def = "{'status': 1, 'expires_at': 1}")
})
public class Broadcast {

    @Id
    private String id;

    @Field("title")
    private String title;

    @Field("message")
    private String message;

    @Indexed
    @Field("target_role")
    private BroadcastTargetRole targetRole;

    @Builder.Default
    @Field("display_modes")
    private Set<BroadcastDisplayMode> displayModes = new HashSet<>();

    @Builder.Default
    @Field("priority")
    private BroadcastPriority priority = BroadcastPriority.NORMAL;

    @Builder.Default
    @Field("category")
    private BroadcastCategory category = BroadcastCategory.GENERAL;

    @Builder.Default
    @Field("is_dismissible")
    private boolean isDismissible = true;

    @Field("action_url")
    private String actionUrl;

    @Field("action_label")
    private String actionLabel;

    @Field("starts_at")
    private Instant startsAt;

    @Field("expires_at")
    private Instant expiresAt;

    @Indexed
    @Builder.Default
    @Field("status")
    private BroadcastStatus status = BroadcastStatus.ACTIVE;

    @Field("created_by")
    private String createdBy;

    @Field("created_at")
    private Instant createdAt;

    @Field("updated_at")
    private Instant updatedAt;
}
