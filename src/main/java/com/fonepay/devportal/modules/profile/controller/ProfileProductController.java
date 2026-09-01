package com.fonepay.devportal.modules.profile.controller;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fonepay.devportal.common.constant.apis.ApiRoutes;
import com.fonepay.devportal.common.dto.ApiResponse;
import com.fonepay.devportal.common.exception.UnauthorizedException;
import com.fonepay.devportal.modules.profile.dto.response.SubscriptionResponse;
import com.fonepay.devportal.modules.profile.service.UserSubscriptionService;
import com.fonepay.devportal.modules.user.document.User;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping(ApiRoutes.Profile.PRODUCTS)
@RequiredArgsConstructor
public class ProfileProductController {

    private final UserSubscriptionService userSubscriptionService;
    private final Clock clock;

    @PostMapping(ApiRoutes.Profile.PRODUCT_BY_ID)
    public ResponseEntity<ApiResponse<SubscriptionResponse>> subscribeProduct(
            @PathVariable @NotBlank String productId,
            Authentication authentication) {

        String userId = extractUserId(authentication);
        log.info("Request to subscribe product [{}] for user [{}]", productId, userId);
        SubscriptionResponse response = userSubscriptionService.subscribeProduct(userId, productId);

        return ResponseEntity.ok(
                ApiResponse.<SubscriptionResponse>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Product subscribed successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    @DeleteMapping(ApiRoutes.Profile.PRODUCT_BY_ID)
    public ResponseEntity<ApiResponse<SubscriptionResponse>> unsubscribeProduct(
            @PathVariable @NotBlank String productId,
            Authentication authentication) {

        String userId = extractUserId(authentication);
        log.info("Request to unsubscribe product [{}] for user [{}]", productId, userId);
        SubscriptionResponse response = userSubscriptionService.unsubscribeProduct(userId, productId);

        return ResponseEntity.ok(
                ApiResponse.<SubscriptionResponse>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Product unsubscribed successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<String>>> getSubscribedProducts(Authentication authentication) {
        String userId = extractUserId(authentication);
        List<String> subscribedProductIds = userSubscriptionService.getSubscribedProductIds(userId);

        return ResponseEntity.ok(
                ApiResponse.<List<String>>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Subscribed products retrieved successfully")
                        .data(subscribedProductIds)
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
        throw new UnauthorizedException("User is unauthenticated or session is invalid");
    }
}
