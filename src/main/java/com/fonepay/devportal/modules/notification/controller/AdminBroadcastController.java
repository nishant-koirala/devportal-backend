package com.fonepay.devportal.modules.notification.controller;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fonepay.devportal.common.constant.apis.ApiRoutes;
import com.fonepay.devportal.common.dto.ApiResponse;
import com.fonepay.devportal.common.dto.PageResponse;
import com.fonepay.devportal.modules.notification.dto.request.BroadcastFilterRequest;
import com.fonepay.devportal.modules.notification.dto.request.CreateBroadcastRequest;
import com.fonepay.devportal.modules.notification.dto.request.UpdateBroadcastRequest;
import com.fonepay.devportal.modules.notification.dto.response.BroadcastMetricsResponse;
import com.fonepay.devportal.modules.notification.dto.response.BroadcastResponse;
import com.fonepay.devportal.modules.notification.service.BroadcastAdminService;
import com.fonepay.devportal.modules.user.document.User;
import org.springframework.security.access.prepost.PreAuthorize;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping(ApiRoutes.Admin.BROADCASTS)
@PreAuthorize("hasAuthority('" + com.fonepay.devportal.security.Permissions.SYSTEM_MANAGE + "')")
@RequiredArgsConstructor
public class AdminBroadcastController {

    private final BroadcastAdminService broadcastAdminService;
    private final Clock clock;

    @PostMapping
    public ResponseEntity<ApiResponse<BroadcastResponse>> createBroadcast(
            @Valid @RequestBody CreateBroadcastRequest request,
            Authentication authentication) {

        String adminId = extractAdminId(authentication);
        BroadcastResponse response = broadcastAdminService.createBroadcast(request, adminId);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<BroadcastResponse>builder()
                        .status(HttpStatus.CREATED.value())
                        .success(true)
                        .message("Broadcast created successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BroadcastResponse>>> getBroadcasts(
            @ModelAttribute BroadcastFilterRequest filter) {

        PageResponse<BroadcastResponse> response = broadcastAdminService.getBroadcasts(filter);

        return ResponseEntity.ok(
                ApiResponse.<PageResponse<BroadcastResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Broadcasts retrieved successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    @GetMapping(ApiRoutes.Admin.BROADCAST_BY_ID)
    public ResponseEntity<ApiResponse<BroadcastResponse>> getBroadcastById(
            @PathVariable @NotBlank String id) {

        BroadcastResponse response = broadcastAdminService.getBroadcastById(id);

        return ResponseEntity.ok(
                ApiResponse.<BroadcastResponse>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Broadcast details retrieved successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    @PutMapping(ApiRoutes.Admin.BROADCAST_BY_ID)
    public ResponseEntity<ApiResponse<BroadcastResponse>> updateBroadcast(
            @PathVariable @NotBlank String id,
            @Valid @RequestBody UpdateBroadcastRequest request,
            Authentication authentication) {

        String adminId = extractAdminId(authentication);
        BroadcastResponse response = broadcastAdminService.updateBroadcast(id, request, adminId);

        return ResponseEntity.ok(
                ApiResponse.<BroadcastResponse>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Broadcast updated successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    @PatchMapping(ApiRoutes.Admin.BROADCAST_CANCEL)
    public ResponseEntity<ApiResponse<BroadcastResponse>> cancelBroadcast(
            @PathVariable @NotBlank String id,
            Authentication authentication) {

        String adminId = extractAdminId(authentication);
        BroadcastResponse response = broadcastAdminService.cancelBroadcast(id, adminId);

        return ResponseEntity.ok(
                ApiResponse.<BroadcastResponse>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Broadcast cancelled successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    @GetMapping(ApiRoutes.Admin.BROADCAST_METRICS)
    public ResponseEntity<ApiResponse<BroadcastMetricsResponse>> getBroadcastMetrics(
            @PathVariable @NotBlank String id) {

        BroadcastMetricsResponse response = broadcastAdminService.getBroadcastMetrics(id);

        return ResponseEntity.ok(
                ApiResponse.<BroadcastMetricsResponse>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Broadcast metrics retrieved successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    private String extractAdminId(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof User user) {
                return user.getUserId();
            }
            if (principal instanceof String str && !"anonymousUser".equalsIgnoreCase(str)) {
                return str;
            }
            if (authentication.getName() != null && !"anonymousUser".equalsIgnoreCase(authentication.getName())) {
                return authentication.getName();
            }
        }
        return "UNKNOWN_ADMIN";
    }
}
