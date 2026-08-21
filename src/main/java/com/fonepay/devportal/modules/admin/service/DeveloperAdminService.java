package com.fonepay.devportal.modules.admin.service;

import com.fonepay.devportal.modules.admin.dto.request.UpdateDeveloperStatusRequest;
import com.fonepay.devportal.modules.admin.dto.response.DeveloperDetailResponse;

public interface DeveloperAdminService {

    DeveloperDetailResponse getDeveloperById(String userId);

    DeveloperDetailResponse updateDeveloperStatus(String userId, UpdateDeveloperStatusRequest request);
}
