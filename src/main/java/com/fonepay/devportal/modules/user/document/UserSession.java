package com.fonepay.devportal.modules.user.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fonepay.devportal.common.constant.enums.SessionStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_sessions")
public class UserSession {

    @Id
    private String sessionId;

    @Field("user_id")
    private String userId;

    @Field("ip_address")
    private String ipAddress;

    @Field("user_agent")
    private String userAgent;

    @Field("created_at")
    private Instant createdAt;

    @Field("last_activity_at")
    private Instant lastActivityAt;

    @Field("expires_at")
    private Instant expiresAt;

    @Field("revoked_at")
    private Instant revokedAt;

    @Field("status")
    private SessionStatus status;
}