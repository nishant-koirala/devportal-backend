package com.fonepay.devportal.modules.admin.developer.service;

import com.fonepay.devportal.common.dto.PageResponse;
import com.fonepay.devportal.modules.admin.developer.dto.request.DeveloperSearchCriteriaDto;
import com.fonepay.devportal.modules.admin.developer.dto.response.DeveloperResponseDto;

public interface DeveloperManagementService {
    PageResponse<DeveloperResponseDto> getDevelopers(DeveloperSearchCriteriaDto criteria);
}
