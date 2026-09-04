package com.fonepay.devportal.modules.cms.controller.public_portal;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;

import com.fonepay.devportal.common.constant.apis.ApiRoutes;
import com.fonepay.devportal.common.dto.ApiResponse;
import com.fonepay.devportal.modules.cms.dto.response.PublicPageResponseDto;
import com.fonepay.devportal.modules.cms.service.PublicPageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PublicPageController {

    private final PublicPageService publicPageService;
    private final Clock clock;

    /**
     * Primary contract route: /api/v1/public/products/{productSlug}/pages/{pageSlug}
     */
    @GetMapping(ApiRoutes.Public.PRODUCTS + ApiRoutes.Public.PRODUCT_PAGE)
    public ResponseEntity<ApiResponse<PublicPageResponseDto>> getPublishedPage(
            @PathVariable String productSlug,
            @PathVariable String pageSlug) {

        String developerId = getDeveloperIdOrThrow();

        log.info("Public request: Fetching published page for productSlug='{}', pageSlug='{}', developerId='{}'", productSlug, pageSlug, developerId);
        PublicPageResponseDto page = publicPageService.getPublishedPage(productSlug, pageSlug, developerId);

        return ResponseEntity.ok(
                ApiResponse.<PublicPageResponseDto>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Published page retrieved successfully")
                        .data(page)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    /**
     * Secondary route: /api/v1/public/pages/{productSlug}/{pageSlug}
     */
    @GetMapping(ApiRoutes.Public.PAGES + "/{productSlug}/{pageSlug}")
    public ResponseEntity<ApiResponse<PublicPageResponseDto>> getPublishedPageByPath(
            @PathVariable String productSlug,
            @PathVariable String pageSlug) {

        return getPublishedPage(productSlug, pageSlug);
    }

    /**
     * Query param route: /api/v1/public/pages?productSlug=...&pageSlug=...
     */
    @GetMapping(ApiRoutes.Public.PAGES)
    public ResponseEntity<ApiResponse<PublicPageResponseDto>> getPublishedPageByQuery(
            @RequestParam String productSlug,
            @RequestParam String pageSlug) {

        return getPublishedPage(productSlug, pageSlug);
    }

    /**
     * FR-316: Documentation Access Guide metadata for visitors
     */
    @GetMapping(ApiRoutes.Public.BASE + "/docs/access-guide")
    public ResponseEntity<ApiResponse<Object>> getAccessGuide() {
        // Here we will eventually return the nested tree using PageTreeBuilder.
        // For now, return a placeholder to satisfy the zero-trust gating.
        return ResponseEntity.ok(
                ApiResponse.<Object>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Documentation access guide metadata")
                        .data(java.util.Collections.emptyList())
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    private String getDeveloperIdOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new com.fonepay.devportal.common.exception.UnauthorizedException("Authentication required to access documentation pages");
        }
        return auth.getName();
    }
}
