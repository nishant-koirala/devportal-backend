package com.fonepay.devportal.modules.admin.developer.activity.dto;

import java.time.Instant;

import com.fonepay.devportal.common.constant.enums.ActivityType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityResponse {

    private String id;
    private String userId;
    private ActivityType type;
    private Instant occurredAt;
}
