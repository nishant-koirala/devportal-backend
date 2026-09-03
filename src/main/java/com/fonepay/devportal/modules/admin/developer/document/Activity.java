package com.fonepay.devportal.modules.admin.developer.document;

import java.time.Instant;

import com.fonepay.devportal.common.constant.enums.ActivityType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
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
@Table(name = "activities")
public class Activity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "user_id", length = 26, nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 32, nullable = false)
    private ActivityType type;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Column(name = "success")
    private Boolean success;
}
