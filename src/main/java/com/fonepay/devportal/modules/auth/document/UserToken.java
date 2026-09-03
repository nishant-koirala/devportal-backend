package com.fonepay.devportal.modules.auth.document;

import java.time.Instant;

import com.fonepay.devportal.common.constant.enums.TokenType;

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
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_tokens")
public class UserToken {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "user_id", length = 26, nullable = false)
    private String userId;

    @Column(name = "token_hash", length = 128, nullable = false, unique = true)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "token_type", length = 32, nullable = false)
    private TokenType tokenType;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Builder.Default
    @Column(name = "attempts", nullable = false)
    private int attempts = 0;
}
