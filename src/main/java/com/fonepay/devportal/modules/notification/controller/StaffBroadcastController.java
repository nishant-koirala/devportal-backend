package com.fonepay.devportal.modules.notification.controller;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fonepay.devportal.common.constant.apis.ApiRoutes;
import com.fonepay.devportal.common.dto.ApiResponse;
import com.fonepay.devportal.common.exception.UnauthorizedException;
import com.fonepay.devportal.modules.notification.dto.response.StaffBroadcastResponse;
import com.fonepay.devportal.modules.notification.dto.response.StaffBroadcastSummaryResponse;
import com.fonepay.devportal.modules.notification.enums.BroadcastDisplayMode;
import com.fonepay.devportal.modules.notification.service.BroadcastSseService;
import com.fonepay.devportal.modules.notification.service.StaffBroadcastService;
import com.fonepay.devportal.modules.user.document.User;
import org.springframework.security.access.prepost.PreAuthorize;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping(ApiRoutes.Staff.BROADCASTS)
@PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
@RequiredArgsConstructor
public class StaffBroadcastController {

    private final StaffBroadcastService staffBroadcastService;
    private final BroadcastSseService broadcastSseService;
    private final Clock clock;

    @GetMapping(value = ApiRoutes.Staff.BROADCAST_STREAM, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamBroadcasts(Authentication authentication) {
        User user = extractUser(authentication);
        log.info("Staff user [{}] requested SSE stream connection", user.getUserId());
        return broadcastSseService.subscribe(user);
    }

    @GetMapping(ApiRoutes.Staff.BROADCAST_ACTIVE)
    public ResponseEntity<ApiResponse<List<StaffBroadcastResponse>>> getActiveBroadcasts(
            @RequestParam(required = false) Set<BroadcastDisplayMode> displayModes,
            @RequestParam(defaultValue = "false") boolean excludeDismissed,
            Authentication authentication) {

        User user = extractUser(authentication);
        List<StaffBroadcastResponse> responses = staffBroadcastService.getActiveBroadcasts(user, displayModes, excludeDismissed);

        return ResponseEntity.ok(
                ApiResponse.<List<StaffBroadcastResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Active broadcasts retrieved successfully")
                        .data(responses)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    @GetMapping(ApiRoutes.Staff.BROADCAST_SUMMARY)
    public ResponseEntity<ApiResponse<StaffBroadcastSummaryResponse>> getSummary(
            Authentication authentication) {

        User user = extractUser(authentication);
        StaffBroadcastSummaryResponse summary = staffBroadcastService.getSummary(user);

        return ResponseEntity.ok(
                ApiResponse.<StaffBroadcastSummaryResponse>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Broadcast summary retrieved successfully")
                        .data(summary)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    @PostMapping(ApiRoutes.Staff.BROADCAST_READ)
    public ResponseEntity<ApiResponse<StaffBroadcastResponse>> markAsRead(
            @PathVariable @NotBlank String id,
            Authentication authentication) {

        User user = extractUser(authentication);
        StaffBroadcastResponse response = staffBroadcastService.markAsRead(id, user.getUserId());

        return ResponseEntity.ok(
                ApiResponse.<StaffBroadcastResponse>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Broadcast marked as read")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    @PostMapping(ApiRoutes.Staff.BROADCAST_READ_ALL)
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            Authentication authentication) {

        User user = extractUser(authentication);
        staffBroadcastService.markAllAsRead(user);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("All active broadcasts marked as read")
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    @PostMapping(ApiRoutes.Staff.BROADCAST_DISMISS)
    public ResponseEntity<ApiResponse<StaffBroadcastResponse>> dismissBroadcast(
            @PathVariable @NotBlank String id,
            Authentication authentication) {

        User user = extractUser(authentication);
        StaffBroadcastResponse response = staffBroadcastService.dismissBroadcast(id, user.getUserId());

        return ResponseEntity.ok(
                ApiResponse.<StaffBroadcastResponse>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Broadcast dismissed successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    private User extractUser(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof User user) {
                return user;
            }
        }
        throw new UnauthorizedException("User session is invalid or unauthenticated");
    }
}
