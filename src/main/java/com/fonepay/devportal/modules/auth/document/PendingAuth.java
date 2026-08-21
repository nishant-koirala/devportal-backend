package com.fonepay.devportal.modules.auth.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fonepay.devportal.common.constant.enums.PendingAuthStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "pending_auths")
public class PendingAuth {

    @Id
    private String id;

    @Indexed
    @Field("user_id")
    private String userId;

    @Field("otp_hash")
    private String otpHash;

    @Field("expires_at")
    private Instant expiresAt;

    @Field("attempts")
    private int attempts;

    @Field("status")
    private PendingAuthStatus status;

    @Field("created_at")
    private Instant createdAt;

    @Field("verified_at")
    private Instant verifiedAt;
}