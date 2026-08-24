package com.fonepay.devportal.modules.admin.developer.activity.controller;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fonepay.devportal.common.constant.apis.ApiRoutes;
import com.fonepay.devportal.common.constant.enums.ActivityType;
import com.fonepay.devportal.common.dto.ApiResponse;
import com.fonepay.devportal.modules.admin.developer.activity.dto.ActivityResponse;
import com.fonepay.devportal.modules.admin.developer.activity.dto.LoginHistoryResponse;
import com.fonepay.devportal.modules.admin.developer.activity.dto.PageResponse;
import com.fonepay.devportal.modules.admin.developer.activity.service.ActivityRecordingService;
import com.fonepay.devportal.security.annotation.RequireAdmin;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiRoutes.Admin.DEVELOPERS)
@RequiredArgsConstructor
@RequireAdmin
public class DeveloperActivityController {

    private final ActivityRecordingService activityRecordingService;
    private final Clock clock;

    @GetMapping("/{userId}/activity")
    public ResponseEntity<ApiResponse<PageResponse<ActivityResponse>>> getActivity(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) ActivityType type) {

        PageResponse<ActivityResponse> data =
                activityRecordingService.getActivity(userId, page, size, sortDirection, type);

        return ResponseEntity.ok(ApiResponse.<PageResponse<ActivityResponse>>builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .message("Developer activity retrieved")
                .data(data)
                .timestamp(LocalDateTime.now(clock))
                .build());
    }

    @GetMapping("/{userId}/login-history")
    public ResponseEntity<ApiResponse<PageResponse<LoginHistoryResponse>>> getLoginHistory(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        PageResponse<LoginHistoryResponse> data =
                activityRecordingService.getLoginHistory(userId, page, size, sortDirection);

        return ResponseEntity.ok(ApiResponse.<PageResponse<LoginHistoryResponse>>builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .message("Login history retrieved")
                .data(data)
                .timestamp(LocalDateTime.now(clock))
                .build());
    }
}
