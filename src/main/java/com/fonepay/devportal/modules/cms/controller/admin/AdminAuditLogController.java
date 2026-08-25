package com.fonepay.devportal.modules.cms.controller.admin;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fonepay.devportal.common.constant.apis.ApiRoutes;
import com.fonepay.devportal.common.dto.ApiResponse;
import com.fonepay.devportal.common.dto.PageResponse;
import com.fonepay.devportal.modules.cms.dto.request.AuditLogSearchCriteriaDto;
import com.fonepay.devportal.modules.cms.dto.response.AuditLogResponseDto;
import com.fonepay.devportal.modules.cms.service.AuditLogService;
import com.fonepay.devportal.security.annotation.RequireAdmin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping(ApiRoutes.Admin.AUDIT_LOGS)
@RequireAdmin
@RequiredArgsConstructor
public class AdminAuditLogController {

    private final AuditLogService auditLogService;
    private final Clock clock;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponseDto>>> getAuditLogs(
            @Valid @ModelAttribute AuditLogSearchCriteriaDto criteria) {

        log.info("Admin querying audit logs with criteria: {}", criteria);
        PageResponse<AuditLogResponseDto> response = auditLogService.getAuditLogs(criteria);

        return ResponseEntity.ok(
                ApiResponse.<PageResponse<AuditLogResponseDto>>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Audit logs retrieved successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }
}
