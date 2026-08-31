package com.fonepay.devportal.modules.notification.service;

import com.fonepay.devportal.common.dto.PageResponse;
import com.fonepay.devportal.modules.notification.dto.request.BroadcastFilterRequest;
import com.fonepay.devportal.modules.notification.dto.request.CreateBroadcastRequest;
import com.fonepay.devportal.modules.notification.dto.request.UpdateBroadcastRequest;
import com.fonepay.devportal.modules.notification.dto.response.BroadcastMetricsResponse;
import com.fonepay.devportal.modules.notification.dto.response.BroadcastResponse;

public interface BroadcastAdminService {

    BroadcastResponse createBroadcast(CreateBroadcastRequest request, String adminId);

    BroadcastResponse updateBroadcast(String broadcastId, UpdateBroadcastRequest request, String adminId);

    BroadcastResponse cancelBroadcast(String broadcastId, String adminId);

    BroadcastResponse getBroadcastById(String broadcastId);

    PageResponse<BroadcastResponse> getBroadcasts(BroadcastFilterRequest filter);

    BroadcastMetricsResponse getBroadcastMetrics(String broadcastId);
}
