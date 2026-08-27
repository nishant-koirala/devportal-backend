package com.fonepay.devportal.modules.notification.service;

import java.util.List;
import java.util.Set;

import com.fonepay.devportal.modules.notification.dto.response.StaffBroadcastResponse;
import com.fonepay.devportal.modules.notification.dto.response.StaffBroadcastSummaryResponse;
import com.fonepay.devportal.modules.notification.enums.BroadcastDisplayMode;
import com.fonepay.devportal.modules.user.document.User;

public interface StaffBroadcastService {

    List<StaffBroadcastResponse> getActiveBroadcasts(User user, Set<BroadcastDisplayMode> displayModes, boolean excludeDismissed);

    StaffBroadcastSummaryResponse getSummary(User user);

    StaffBroadcastResponse markAsRead(String broadcastId, String userId);

    void markAllAsRead(User user);

    StaffBroadcastResponse dismissBroadcast(String broadcastId, String userId);
}
