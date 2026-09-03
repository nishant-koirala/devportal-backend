package com.fonepay.devportal.modules.product.service;

import java.util.List;

import com.fonepay.devportal.modules.product.document.DocTreeNodeSnapshot;
import com.fonepay.devportal.modules.product.document.PageSnapshot;
import com.fonepay.devportal.modules.product.dto.request.CreateProductVersionRequest;
import com.fonepay.devportal.modules.product.dto.request.UpdateProductVersionRequest;
import com.fonepay.devportal.modules.product.dto.response.ProductVersionResponseDto;
import com.fonepay.devportal.modules.product.dto.response.ProductVersionSummaryDto;

public interface ProductVersionService {

    /**
     * Snapshots the active published documentation tree and pages for the product.
     */
    ProductVersionResponseDto createSnapshotFromLiveProduct(String productId, CreateProductVersionRequest request, String userId);

    /**
     * Creates a manual or custom product release version.
     */
    ProductVersionResponseDto createProductVersion(String productId, CreateProductVersionRequest request, String userId);

    /**
     * Lists all release version summaries for a product (used by version switcher UI).
     */
    List<ProductVersionSummaryDto> getVersionsByProduct(String productIdOrSlug);

    /**
     * Retrieves full version details by product slug and version slug (e.g., /products/qr/versions/v18).
     */
    ProductVersionResponseDto getVersionBySlug(String productSlug, String versionSlug);

    /**
     * Retrieves the latest active release version for a product.
     */
    ProductVersionResponseDto getLatestVersion(String productSlug);

    /**
     * Retrieves the sidebar navigation tree snapshot for a specific version.
     */
    List<DocTreeNodeSnapshot> getVersionNavigationTree(String productSlug, String versionSlug);

    /**
     * Retrieves an immutable page snapshot with its content blocks from a specific version.
     */
    PageSnapshot getVersionPageSnapshot(String productSlug, String versionSlug, String pageSlug);

    /**
     * Updates version metadata (deprecation banner, release notes, latest/default status).
     */
    ProductVersionResponseDto updateProductVersion(String versionId, UpdateProductVersionRequest request, String userId);

    /**
     * Deletes a product version.
     */
    void deleteProductVersion(String versionId);
}
