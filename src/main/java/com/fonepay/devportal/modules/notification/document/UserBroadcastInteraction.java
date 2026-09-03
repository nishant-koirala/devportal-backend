package com.fonepay.devportal.modules.notification.document;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

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
@Table(name = "broadcast_interactions", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "broadcast_id" }))
public class UserBroadcastInteraction {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "user_id", length = 26, nullable = false)
    private String userId;

    @Column(name = "broadcast_id", length = 26, nullable = false)
    private String broadcastId;

    @Builder.Default
    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Column(name = "read_at")
    private Instant readAt;

    @Builder.Default
    @Column(name = "is_dismissed", nullable = false)
    private boolean isDismissed = false;

    @Column(name = "dismissed_at")
    private Instant dismissedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
