package com.fonepay.devportal.modules.admin.controller;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fonepay.devportal.common.constant.apis.ApiRoutes;
import com.fonepay.devportal.common.dto.ApiResponse;
import com.fonepay.devportal.common.dto.PageResponse;
import com.fonepay.devportal.modules.admin.developer.dto.request.DeveloperSearchCriteriaDto;
import com.fonepay.devportal.modules.admin.developer.dto.response.DeveloperResponseDto;
import com.fonepay.devportal.modules.admin.developer.service.DeveloperManagementService;
import com.fonepay.devportal.modules.admin.dto.request.UpdateDeveloperStatusRequest;
import com.fonepay.devportal.modules.admin.dto.response.DeveloperDetailResponse;
import com.fonepay.devportal.modules.admin.service.DeveloperAdminService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping(ApiRoutes.Admin.DEVELOPERS)
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminDeveloperController {

    private final DeveloperAdminService developerAdminService;
    private final DeveloperManagementService developerManagementService;
    private final Clock clock;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<DeveloperResponseDto>>> getDevelopers(
            @Valid @ModelAttribute DeveloperSearchCriteriaDto criteria) {
        log.info("Admin fetching developers list with criteria: {}", criteria);
        PageResponse<DeveloperResponseDto> response = developerManagementService.getDevelopers(criteria);

        return ResponseEntity.ok(ApiResponse.<PageResponse<DeveloperResponseDto>>builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .message("Developers retrieved successfully")
                .data(response)
                .timestamp(LocalDateTime.now(clock))
                .build());
    }

    @GetMapping(ApiRoutes.Admin.DEVELOPER_BY_ID)
    public ResponseEntity<ApiResponse<DeveloperDetailResponse>> getDeveloperById(@PathVariable String userId) {
        log.info("Admin fetching developer details for ID: {}", userId);
        DeveloperDetailResponse response = developerAdminService.getDeveloperById(userId);

        return ResponseEntity.ok(ApiResponse.<DeveloperDetailResponse>builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .message("Developer profile retrieved successfully")
                .data(response)
                .timestamp(LocalDateTime.now(clock))
                .build());
    }

    @PatchMapping(ApiRoutes.Admin.DEVELOPER_STATUS)
    public ResponseEntity<ApiResponse<DeveloperDetailResponse>> updateDeveloperStatus(
            @PathVariable String userId,
            @Valid @RequestBody UpdateDeveloperStatusRequest request) {
        log.info("Admin updating developer status for ID: {} to {}", userId, request.getStatus());
        DeveloperDetailResponse response = developerAdminService.updateDeveloperStatus(userId, request);

        return ResponseEntity.ok(ApiResponse.<DeveloperDetailResponse>builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .message("Developer status updated successfully")
                .data(response)
                .timestamp(LocalDateTime.now(clock))
                .build());
    }
}
