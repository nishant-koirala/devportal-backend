package com.fonepay.devportal.modules.user.controller;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fonepay.devportal.common.constant.apis.ApiRoutes;
import com.fonepay.devportal.common.dto.ApiResponse;
import com.fonepay.devportal.modules.user.dto.request.EmailChangeRequest;
import com.fonepay.devportal.modules.user.dto.request.UpdatePasswordRequest;
import com.fonepay.devportal.modules.user.dto.request.UpdateProfileRequest;
import com.fonepay.devportal.modules.user.service.UserProfileService;
import com.fonepay.devportal.modules.user.document.User;
import org.springframework.security.core.Authentication;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiRoutes.Profile.BASE)
@RequiredArgsConstructor
public class DeveloperProfileController {

    private final UserProfileService userProfileService;
    private final Clock clock;

    @PutMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        
        String userId = extractUserId(authentication);
        userProfileService.updateProfile(userId, request);
        
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .message("Profile updated successfully")
                .timestamp(LocalDateTime.now(clock))
                .build());
    }

    @PutMapping(ApiRoutes.Profile.PASSWORD)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            Authentication authentication,
            @Valid @RequestBody UpdatePasswordRequest request) {
        
        String userId = extractUserId(authentication);
        userProfileService.updatePassword(userId, request);
        
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .message("Password updated successfully. You have been logged out of all devices.")
                .timestamp(LocalDateTime.now(clock))
                .build());
    }

    @PostMapping(ApiRoutes.Profile.EMAIL_REQUEST)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> requestEmailChange(
            Authentication authentication,
            @Valid @RequestBody EmailChangeRequest request) {
        
        String userId = extractUserId(authentication);
        userProfileService.requestEmailChange(userId, request);
        
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .message("Verification email sent to your new email address. Please click the link to complete the change.")
                .timestamp(LocalDateTime.now(clock))
                .build());
    }

    @GetMapping(ApiRoutes.Profile.EMAIL_VERIFY)
    public ResponseEntity<ApiResponse<Void>> verifyEmailChange(
            @RequestParam("token") String token) {
        
        userProfileService.verifyEmailChange(token);
        
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .message("Email address successfully changed.")
                .timestamp(LocalDateTime.now(clock))
                .build());
    }

    private String extractUserId(Authentication authentication) {
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
        throw new com.fonepay.devportal.common.exception.UnauthorizedException("User not found in security context");
    }
}
