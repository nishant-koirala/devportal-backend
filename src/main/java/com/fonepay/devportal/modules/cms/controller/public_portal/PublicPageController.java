package com.fonepay.devportal.modules.cms.controller.public_portal;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

        log.info("Public request: Fetching published page for productSlug='{}', pageSlug='{}'", productSlug, pageSlug);
        PublicPageResponseDto page = publicPageService.getPublishedPage(productSlug, pageSlug);

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
}
