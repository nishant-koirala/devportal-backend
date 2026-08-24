package com.fonepay.devportal.modules.admin.developer.dto.response;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Admin login-history API row: one login attempt, success or fail. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginHistoryResponse {

    private String id;
    private String userId;
    private Instant loginAt;
    private String ipAddress;
    private String userAgent;
    private boolean success;
}
