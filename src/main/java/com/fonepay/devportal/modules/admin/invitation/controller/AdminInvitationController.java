package com.fonepay.devportal.modules.admin.invitation.controller;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fonepay.devportal.common.constant.apis.ApiRoutes;
import com.fonepay.devportal.common.dto.ApiResponse;
import com.fonepay.devportal.modules.admin.invitation.dto.request.CreateInvitationRequest;
import com.fonepay.devportal.modules.admin.invitation.dto.response.InvitationResponse;
import com.fonepay.devportal.modules.admin.invitation.service.InvitationService;
import com.fonepay.devportal.modules.user.document.User;
import com.fonepay.devportal.security.annotation.RequireAdmin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping(ApiRoutes.Admin.INVITATIONS)
@RequireAdmin
@RequiredArgsConstructor
public class AdminInvitationController {

    private final InvitationService invitationService;
    private final Clock clock;

    @PostMapping
    public ResponseEntity<ApiResponse<InvitationResponse>> invite(
            @Valid @RequestBody CreateInvitationRequest request,
            Authentication authentication) {

        String adminId = extractAdminId(authentication);
        log.info("Admin [{}] inviting {} as {}", adminId, request.getEmail(), request.getRole());

        InvitationResponse response = invitationService.invite(request, adminId);
        HttpStatus status = response.isResent() ? HttpStatus.OK : HttpStatus.CREATED;
        String message = response.isResent()
                ? "Invitation resent successfully"
                : "Invitation sent successfully";

        return ResponseEntity.status(status).body(
                ApiResponse.<InvitationResponse>builder()
                        .status(status.value())
                        .success(true)
                        .message(message)
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
