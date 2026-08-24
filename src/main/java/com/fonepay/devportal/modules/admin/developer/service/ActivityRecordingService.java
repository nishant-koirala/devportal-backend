package com.fonepay.devportal.modules.admin.developer.service;

import com.fonepay.devportal.common.constant.enums.ActivityType;
import com.fonepay.devportal.modules.admin.developer.dto.ActivityResponse;
import com.fonepay.devportal.modules.admin.developer.dto.LoginHistoryResponse;
import com.fonepay.devportal.modules.admin.developer.dto.PageResponse;

/** Writes activity when auth events happen; admin GET APIs read the same collection. */
public interface ActivityRecordingService {

    /** Non-login events (LOGOUT, EMAIL_VERIFIED, PASSWORD_RESET). */
    void record(String userId, ActivityType type);

    /** Login attempt: also stores IP, user agent, and success/fail. */
    void recordLogin(String userId, String ipAddress, String userAgent, boolean success);

    PageResponse<ActivityResponse> getActivity(
            String userId, int page, int size, String sortDirection, ActivityType type);

    PageResponse<LoginHistoryResponse> getLoginHistory(
            String userId, int page, int size, String sortDirection);
}
