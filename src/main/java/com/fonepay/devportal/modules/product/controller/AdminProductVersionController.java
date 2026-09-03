package com.fonepay.devportal.modules.product.controller;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fonepay.devportal.common.dto.ApiResponse;
import com.fonepay.devportal.modules.product.dto.request.CreateProductVersionRequest;
import com.fonepay.devportal.modules.product.dto.request.UpdateProductVersionRequest;
import com.fonepay.devportal.modules.product.dto.response.ProductVersionResponseDto;
import com.fonepay.devportal.modules.product.dto.response.ProductVersionSummaryDto;
import com.fonepay.devportal.modules.product.service.ProductVersionService;
import com.fonepay.devportal.security.Permissions;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/admin/products/{productId}/versions")
@PreAuthorize("hasAuthority('" + Permissions.PRODUCT_MANAGE + "')")
@RequiredArgsConstructor
public class AdminProductVersionController {

    private final ProductVersionService productVersionService;
    private final Clock clock;

    /**
     * Snapshots the live published pages & navigation hierarchy of a product into a major release version.
     * POST /api/v1/admin/products/{productId}/versions/snapshot
     */
    @PostMapping("/snapshot")
    public ResponseEntity<ApiResponse<ProductVersionResponseDto>> createSnapshot(
            @PathVariable String productId,
            @Valid @RequestBody CreateProductVersionRequest request,
            Authentication authentication) {

        String userId = authentication != null ? authentication.getName() : "system";
        log.info("Admin [{}] creating snapshot for product '{}' with version '{}'", userId, productId, request.getVersionName());

        ProductVersionResponseDto response = productVersionService.createSnapshotFromLiveProduct(productId, request, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<ProductVersionResponseDto>builder()
                        .status(HttpStatus.CREATED.value())
                        .success(true)
                        .message("Product version documentation snapshot created successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    /**
     * Creates a custom/manual product version.
     * POST /api/v1/admin/products/{productId}/versions
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ProductVersionResponseDto>> createVersion(
            @PathVariable String productId,
            @Valid @RequestBody CreateProductVersionRequest request,
            Authentication authentication) {

        String userId = authentication != null ? authentication.getName() : "system";
        log.info("Admin [{}] creating manual version for product '{}'", userId, productId);

        ProductVersionResponseDto response = productVersionService.createProductVersion(productId, request, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<ProductVersionResponseDto>builder()
                        .status(HttpStatus.CREATED.value())
                        .success(true)
                        .message("Product version created successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    /**
     * Lists all versions for a product.
     * GET /api/v1/admin/products/{productId}/versions
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductVersionSummaryDto>>> getVersions(
            @PathVariable String productId) {

        List<ProductVersionSummaryDto> versions = productVersionService.getVersionsByProduct(productId);

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
     * Updates product version metadata (deprecation banner, release notes, latest/LTS flags).
     * PUT /api/v1/admin/products/{productId}/versions/{versionId}
     */
    @PutMapping("/{versionId}")
    public ResponseEntity<ApiResponse<ProductVersionResponseDto>> updateVersion(
            @PathVariable String productId,
            @PathVariable String versionId,
            @Valid @RequestBody UpdateProductVersionRequest request,
            Authentication authentication) {

        String userId = authentication != null ? authentication.getName() : "system";
        ProductVersionResponseDto updated = productVersionService.updateProductVersion(versionId, request, userId);

        return ResponseEntity.ok(
                ApiResponse.<ProductVersionResponseDto>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Product version updated successfully")
                        .data(updated)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    /**
     * Deletes a mutable product version.
     * DELETE /api/v1/admin/products/{productId}/versions/{versionId}
     */
    @DeleteMapping("/{versionId}")
    public ResponseEntity<ApiResponse<Void>> deleteVersion(
            @PathVariable String productId,
            @PathVariable String versionId) {

        productVersionService.deleteProductVersion(versionId);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Product version deleted successfully")
                        .data(null)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }
}
