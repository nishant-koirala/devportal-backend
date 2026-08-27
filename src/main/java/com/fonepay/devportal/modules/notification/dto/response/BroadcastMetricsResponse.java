package com.fonepay.devportal.modules.notification.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BroadcastMetricsResponse {

    private String broadcastId;
    private long totalReadCount;
    private long totalDismissedCount;
}
