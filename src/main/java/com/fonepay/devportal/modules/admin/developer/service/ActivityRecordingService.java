package com.fonepay.devportal.modules.admin.developer.service;

import com.fonepay.devportal.common.constant.enums.ActivityType;
import com.fonepay.devportal.modules.admin.developer.dto.response.ActivityResponse;
import com.fonepay.devportal.modules.admin.developer.dto.response.LoginHistoryResponse;
import com.fonepay.devportal.modules.admin.developer.dto.response.PageResponse;

public interface ActivityRecordingService {

        void record(String userId, ActivityType type);

        void recordLogin(String userId, String ipAddress, String userAgent, boolean success);

        PageResponse<ActivityResponse> getActivity(
                        String userId, int page, int size, String sortDirection, ActivityType type);

        PageResponse<LoginHistoryResponse> getLoginHistory(
                        String userId, int page, int size, String sortDirection);
}
