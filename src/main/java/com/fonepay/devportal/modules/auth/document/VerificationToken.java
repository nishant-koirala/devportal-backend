package com.fonepay.devportal.modules.auth.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "verification_tokens")
public class VerificationToken {

    @Id
    private String id;

    @Indexed
    private String token;

    @Indexed
    private String userId;

    private Instant createdAt;

    private Instant expiryDate;
}
