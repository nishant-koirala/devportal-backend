package com.fonepay.devportal.modules.notification.document;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import com.fonepay.devportal.modules.notification.enums.BroadcastCategory;
import com.fonepay.devportal.modules.notification.enums.BroadcastDisplayMode;
import com.fonepay.devportal.modules.notification.enums.BroadcastPriority;
import com.fonepay.devportal.modules.notification.enums.BroadcastStatus;
import com.fonepay.devportal.modules.notification.enums.BroadcastTargetRole;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "broadcasts")
public class Broadcast {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_role", length = 32, nullable = false)
    private BroadcastTargetRole targetRole;

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "broadcast_display_modes", joinColumns = @JoinColumn(name = "broadcast_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "display_mode", length = 32, nullable = false)
    private Set<BroadcastDisplayMode> displayModes = new HashSet<>();

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 32, nullable = false)
    private BroadcastPriority priority = BroadcastPriority.NORMAL;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 32, nullable = false)
    private BroadcastCategory category = BroadcastCategory.GENERAL;

    @Builder.Default
    @Column(name = "is_dismissible", nullable = false)
    private boolean isDismissible = true;

    @Column(name = "action_url", length = 2048)
    private String actionUrl;

    @Column(name = "action_label")
    private String actionLabel;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32, nullable = false)
    private BroadcastStatus status = BroadcastStatus.ACTIVE;

    @Column(name = "created_by", length = 26, nullable = false, columnDefinition = "CHAR(26)")
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
