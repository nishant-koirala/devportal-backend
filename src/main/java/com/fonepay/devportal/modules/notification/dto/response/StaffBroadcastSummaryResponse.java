package com.fonepay.devportal.modules.notification.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffBroadcastSummaryResponse {

    private long unreadCount;
    private long activeBannerCount;
}
