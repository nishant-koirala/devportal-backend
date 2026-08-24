package com.fonepay.devportal.modules.admin.developer.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
