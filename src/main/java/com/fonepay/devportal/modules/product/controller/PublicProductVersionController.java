package com.fonepay.devportal.modules.product.controller;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fonepay.devportal.common.dto.ApiResponse;
import com.fonepay.devportal.modules.product.document.DocTreeNodeSnapshot;
import com.fonepay.devportal.modules.product.document.PageSnapshot;
import com.fonepay.devportal.modules.product.dto.response.ProductVersionResponseDto;
import com.fonepay.devportal.modules.product.dto.response.ProductVersionSummaryDto;
import com.fonepay.devportal.modules.product.service.ProductVersionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/public/products/{productSlug}/versions")
@RequiredArgsConstructor
public class PublicProductVersionController {

    private final ProductVersionService productVersionService;
    private final Clock clock;

    /**
     * Lists all available versions for a product (used by the angular.dev style version picker).
     * GET /api/v1/public/products/{productSlug}/versions
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductVersionSummaryDto>>> getVersions(
            @PathVariable String productSlug) {

        log.info("Public request: Fetching all versions for product '{}'", productSlug);
        List<ProductVersionSummaryDto> versions = productVersionService.getVersionsByProduct(productSlug);

        return ResponseEntity.ok(
                ApiResponse.<List<ProductVersionSummaryDto>>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Product versions retrieved successfully")
                        .data(versions)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    /**
     * Retrieves the latest active release version for a product.
     * GET /api/v1/public/products/{productSlug}/versions/latest
     */
    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<ProductVersionResponseDto>> getLatestVersion(
            @PathVariable String productSlug) {

        log.info("Public request: Fetching latest version for product '{}'", productSlug);
        ProductVersionResponseDto latest = productVersionService.getLatestVersion(productSlug);

        return ResponseEntity.ok(
                ApiResponse.<ProductVersionResponseDto>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Latest product version retrieved successfully")
                        .data(latest)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    /**
     * Retrieves version details and snapshot hierarchy by version slug.
     * GET /api/v1/public/products/{productSlug}/versions/{versionSlug}
     */
    @GetMapping("/{versionSlug}")
    public ResponseEntity<ApiResponse<ProductVersionResponseDto>> getVersion(
            @PathVariable String productSlug,
            @PathVariable String versionSlug) {

        log.info("Public request: Fetching version '{}' for product '{}'", versionSlug, productSlug);
        ProductVersionResponseDto version = productVersionService.getVersionBySlug(productSlug, versionSlug);

        return ResponseEntity.ok(
                ApiResponse.<ProductVersionResponseDto>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Product version details retrieved successfully")
                        .data(version)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    /**
     * Retrieves the navigation sidebar tree snapshot for a specific version.
     * GET /api/v1/public/products/{productSlug}/versions/{versionSlug}/tree
     */
    @GetMapping("/{versionSlug}/tree")
    public ResponseEntity<ApiResponse<List<DocTreeNodeSnapshot>>> getVersionTree(
            @PathVariable String productSlug,
            @PathVariable String versionSlug) {

        log.info("Public request: Fetching navigation tree for version '{}' of product '{}'", versionSlug, productSlug);
        List<DocTreeNodeSnapshot> tree = productVersionService.getVersionNavigationTree(productSlug, versionSlug);

        return ResponseEntity.ok(
                ApiResponse.<List<DocTreeNodeSnapshot>>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Navigation tree retrieved successfully")
                        .data(tree)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    /**
     * Retrieves a snapshotted documentation page with its content blocks for a specific version.
     * GET /api/v1/public/products/{productSlug}/versions/{versionSlug}/pages/{pageSlug}
     */
    @GetMapping("/{versionSlug}/pages/{pageSlug}")
    public ResponseEntity<ApiResponse<PageSnapshot>> getVersionPage(
            @PathVariable String productSlug,
            @PathVariable String versionSlug,
            @PathVariable String pageSlug) {

        log.info("Public request: Fetching page '{}' from version '{}' of product '{}'", pageSlug, versionSlug, productSlug);
        PageSnapshot page = productVersionService.getVersionPageSnapshot(productSlug, versionSlug, pageSlug);

        return ResponseEntity.ok(
                ApiResponse.<PageSnapshot>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Snapshotted page retrieved successfully")
                        .data(page)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }
}
