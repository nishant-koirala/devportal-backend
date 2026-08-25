package com.fonepay.devportal.modules.cms.dto.response;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponseDto {
    private String id;
    private String adminId;
    private String action;
    private String targetId;
    private String targetType;
    private String sourceIp;
    private Instant timestamp;
}
