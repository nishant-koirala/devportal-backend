package com.fonepay.devportal.modules.admin.developer.service;

import com.fonepay.devportal.modules.admin.developer.dto.request.UpdateDeveloperStatusRequest;
import com.fonepay.devportal.modules.admin.developer.dto.response.DeveloperDetailResponse;

public interface DeveloperAdminService {

    DeveloperDetailResponse getDeveloperById(String userId);

    DeveloperDetailResponse updateDeveloperStatus(String userId, UpdateDeveloperStatusRequest request);
}
