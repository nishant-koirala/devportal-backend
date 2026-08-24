package com.fonepay.devportal.modules.admin.developer.document;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fonepay.devportal.common.constant.enums.ActivityType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One append-only event for a developer (activity timeline + login attempts).
 * Login rows also store IP, user agent, and success; other types leave those null.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "activities")
@CompoundIndex(name = "user_occurred_idx", def = "{'user_id': 1, 'occurred_at': -1}")
public class Activity {

    @Id
    private String id;

    @Field("user_id")
    private String userId;

    @Field("type")
    private ActivityType type;

    @Field("occurred_at")
    private Instant occurredAt;

    /** Set only for LOGIN rows. */
    @Field("ip_address")
    private String ipAddress;

    /** Set only for LOGIN rows. */
    @Field("user_agent")
    private String userAgent;

    /** Set only for LOGIN rows: true = success, false = failed attempt. */
    @Field("success")
    private Boolean success;
}
