package com.fonepay.devportal.modules.cms.service;

import com.fonepay.devportal.common.dto.PageResponse;
import com.fonepay.devportal.modules.cms.document.AuditLog;
import com.fonepay.devportal.modules.cms.dto.request.AuditLogSearchCriteriaDto;
import com.fonepay.devportal.modules.cms.dto.response.AuditLogResponseDto;

public interface AuditLogService {

    AuditLog logAction(String adminId, String action, String targetId, String targetType, String sourceIp);

    AuditLog logAction(String action, String targetId, String targetType);

    PageResponse<AuditLogResponseDto> getAuditLogs(AuditLogSearchCriteriaDto criteria);
}
