package com.fonepay.devportal.modules.product.service.impl;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fonepay.devportal.common.exception.BadRequestException;
import com.fonepay.devportal.common.exception.DuplicateResourceException;
import com.fonepay.devportal.common.exception.ResourceNotFoundException;
import com.fonepay.devportal.modules.cms.document.Page;
import com.fonepay.devportal.modules.cms.document.Product;
import com.fonepay.devportal.modules.cms.document.ProductResource;
import com.fonepay.devportal.modules.cms.enums.PageStatus;
import com.fonepay.devportal.modules.cms.repository.PageRepository;
import com.fonepay.devportal.modules.cms.repository.ProductRepository;
import com.fonepay.devportal.modules.product.document.DocTreeNodeSnapshot;
import com.fonepay.devportal.modules.product.document.PageSnapshot;
import com.fonepay.devportal.modules.product.document.ProductVersion;
import com.fonepay.devportal.modules.product.document.VersionResource;
import com.fonepay.devportal.modules.product.dto.request.CreateProductVersionRequest;
import com.fonepay.devportal.modules.product.dto.request.UpdateProductVersionRequest;
import com.fonepay.devportal.modules.product.dto.response.ProductVersionResponseDto;
import com.fonepay.devportal.modules.product.dto.response.ProductVersionSummaryDto;
import com.fonepay.devportal.modules.product.enums.DocNodeType;
import com.fonepay.devportal.modules.product.enums.ProductVersionStatus;
import com.fonepay.devportal.modules.product.repository.ProductVersionRepository;
import com.fonepay.devportal.modules.product.service.ProductVersionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductVersionServiceImpl implements ProductVersionService {

    private static final Comparator<Page> SIBLING_ORDER = Comparator
            .comparingInt(Page::getPageOrder)
            .thenComparing(Page::getTitle, Comparator.nullsLast(String::compareToIgnoreCase));

    private final ProductVersionRepository productVersionRepository;
    private final ProductRepository productRepository;
    private final PageRepository pageRepository;
    private final Clock clock;

    @Override
    public ProductVersionResponseDto createSnapshotFromLiveProduct(String productId, CreateProductVersionRequest request, String userId) {
        Product product = findProductByIdOrSlug(productId);
        String versionSlug = resolveSlug(request.getSlug(), request.getVersionName());

        validateVersionUniqueness(product.getId(), request.getVersionName(), versionSlug, null);

        // 1. Fetch published pages
        List<Page> livePages = pageRepository.findByProductIdOrderByPageOrderAsc(product.getId());
        List<Page> publishedPages = livePages.stream()
                .filter(p -> p.getStatus() == PageStatus.PUBLISHED)
                .toList();

        // 2. Build hierarchical sidebar navigation tree snapshot
        List<DocTreeNodeSnapshot> navigationTree = buildNavigationTree(publishedPages, product.getSlug(), versionSlug);

        // 3. Build immutable page snapshots
        List<PageSnapshot> pageSnapshots = buildPageSnapshots(publishedPages, product.getSlug(), versionSlug);

        // 4. Snapshot developer resources
        List<VersionResource> resources = snapshotResources(product.getResources());

        Instant now = Instant.now(clock);

        if (request.isLatest()) {
            demoteOtherLatestVersions(product.getId());
        }

        ProductVersion productVersion = ProductVersion.builder()
                .productId(product.getId())
                .productSlug(product.getSlug())
                .versionName(request.getVersionName().trim())
                .slug(versionSlug)
                .displayTitle(request.getDisplayTitle() != null && !request.getDisplayTitle().isBlank()
                        ? request.getDisplayTitle().trim()
                        : request.getVersionName().trim())
                .status(request.getStatus() != null ? request.getStatus() : ProductVersionStatus.ACTIVE)
                .isLatest(request.isLatest())
                .isDefault(request.isDefault())
                .isLts(request.isLts())
                .isDeprecated(request.isDeprecated())
                .deprecationNotice(request.getDeprecationNotice())
                .displayOrder(request.getDisplayOrder())
                .releaseNotes(request.getReleaseNotes())
                .changelogUrl(request.getChangelogUrl())
                .apiSpecUrl(request.getApiSpecUrl())
                .releasedAt(request.getReleasedAt() != null ? request.getReleasedAt() : now)
                .endOfLifeAt(request.getEndOfLifeAt())
                .navigationTree(navigationTree)
                .pageSnapshots(pageSnapshots)
                .resources(resources)
                .metadata(request.getMetadata() != null ? request.getMetadata() : new HashMap<>())
                .isImmutable(request.isImmutable())
                .commitMessage(request.getCommitMessage())
                .snapshottedAt(now)
                .snapshottedBy(userId)
                .createdAt(now)
                .updatedAt(now)
                .createdBy(userId)
                .updatedBy(userId)
                .build();

        ProductVersion saved = productVersionRepository.save(productVersion);
        log.info("Created documentation snapshot version '{}' ({}) for product '{}'", saved.getVersionName(), saved.getSlug(), product.getSlug());
        return mapToResponseDto(saved);
    }

    @Override
    public ProductVersionResponseDto createProductVersion(String productId, CreateProductVersionRequest request, String userId) {
        Product product = findProductByIdOrSlug(productId);
        String versionSlug = resolveSlug(request.getSlug(), request.getVersionName());

        validateVersionUniqueness(product.getId(), request.getVersionName(), versionSlug, null);

        if (request.isLatest()) {
            demoteOtherLatestVersions(product.getId());
        }

        Instant now = Instant.now(clock);

        ProductVersion productVersion = ProductVersion.builder()
                .productId(product.getId())
                .productSlug(product.getSlug())
                .versionName(request.getVersionName().trim())
                .slug(versionSlug)
                .displayTitle(request.getDisplayTitle() != null && !request.getDisplayTitle().isBlank()
                        ? request.getDisplayTitle().trim()
                        : request.getVersionName().trim())
                .status(request.getStatus() != null ? request.getStatus() : ProductVersionStatus.ACTIVE)
                .isLatest(request.isLatest())
                .isDefault(request.isDefault())
                .isLts(request.isLts())
                .isDeprecated(request.isDeprecated())
                .deprecationNotice(request.getDeprecationNotice())
                .displayOrder(request.getDisplayOrder())
                .releaseNotes(request.getReleaseNotes())
                .changelogUrl(request.getChangelogUrl())
                .apiSpecUrl(request.getApiSpecUrl())
                .releasedAt(request.getReleasedAt() != null ? request.getReleasedAt() : now)
                .endOfLifeAt(request.getEndOfLifeAt())
                .navigationTree(request.getNavigationTree() != null ? request.getNavigationTree() : new ArrayList<>())
                .pageSnapshots(request.getPageSnapshots() != null ? request.getPageSnapshots() : new ArrayList<>())
                .resources(request.getResources() != null ? request.getResources() : new ArrayList<>())
                .metadata(request.getMetadata() != null ? request.getMetadata() : new HashMap<>())
                .isImmutable(request.isImmutable())
                .commitMessage(request.getCommitMessage())
                .snapshottedAt(now)
                .snapshottedBy(userId)
                .createdAt(now)
                .updatedAt(now)
                .createdBy(userId)
                .updatedBy(userId)
                .build();

        ProductVersion saved = productVersionRepository.save(productVersion);
        return mapToResponseDto(saved);
    }

    @Override
    public List<ProductVersionSummaryDto> getVersionsByProduct(String productIdOrSlug) {
        Product product = findProductByIdOrSlug(productIdOrSlug);
        List<ProductVersion> versions = productVersionRepository.findByProductIdOrderByDisplayOrderAsc(product.getId());

        return versions.stream()
                .map(this::mapToSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    public ProductVersionResponseDto getVersionBySlug(String productSlug, String versionSlug) {
        ProductVersion version = productVersionRepository.findByProductSlugAndSlug(productSlug, versionSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Version '" + versionSlug + "' not found for product '" + productSlug + "'"));

        return mapToResponseDto(version);
    }

    @Override
    public ProductVersionResponseDto getLatestVersion(String productSlug) {
        ProductVersion version = productVersionRepository.findByProductSlugAndIsLatestTrue(productSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Latest version not found for product '" + productSlug + "'"));

        return mapToResponseDto(version);
    }

    @Override
    public List<DocTreeNodeSnapshot> getVersionNavigationTree(String productSlug, String versionSlug) {
        ProductVersion version = productVersionRepository.findByProductSlugAndSlug(productSlug, versionSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Version '" + versionSlug + "' not found for product '" + productSlug + "'"));

        return version.getNavigationTree() != null ? version.getNavigationTree() : List.of();
    }

    @Override
    public PageSnapshot getVersionPageSnapshot(String productSlug, String versionSlug, String pageSlug) {
        ProductVersion version = productVersionRepository.findByProductSlugAndSlug(productSlug, versionSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Version '" + versionSlug + "' not found for product '" + productSlug + "'"));

        return version.getPageSnapshots().stream()
                .filter(p -> pageSlug.equalsIgnoreCase(p.getSlug()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Page '" + pageSlug + "' not found in version '" + versionSlug + "'"));
    }

    @Override
    public ProductVersionResponseDto updateProductVersion(String versionId, UpdateProductVersionRequest request, String userId) {
        ProductVersion version = productVersionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Product version not found with id: " + versionId));

        if (version.isImmutable() && Boolean.FALSE.equals(request.getIsImmutable())) {
            log.warn("Unlocking immutable product version {}", versionId);
        }

        if (request.getDisplayTitle() != null) version.setDisplayTitle(request.getDisplayTitle().trim());
        if (request.getStatus() != null) version.setStatus(request.getStatus());
        if (request.getIsLatest() != null) {
            if (request.getIsLatest()) {
                demoteOtherLatestVersions(version.getProductId());
            }
            version.setLatest(request.getIsLatest());
        }
        if (request.getIsDefault() != null) version.setDefault(request.getIsDefault());
        if (request.getIsLts() != null) version.setLts(request.getIsLts());
        if (request.getIsDeprecated() != null) version.setDeprecated(request.getIsDeprecated());
        if (request.getDeprecationNotice() != null) version.setDeprecationNotice(request.getDeprecationNotice());
        if (request.getDisplayOrder() != null) version.setDisplayOrder(request.getDisplayOrder());
        if (request.getReleaseNotes() != null) version.setReleaseNotes(request.getReleaseNotes());
        if (request.getChangelogUrl() != null) version.setChangelogUrl(request.getChangelogUrl());
        if (request.getApiSpecUrl() != null) version.setApiSpecUrl(request.getApiSpecUrl());
        if (request.getReleasedAt() != null) version.setReleasedAt(request.getReleasedAt());
        if (request.getEndOfLifeAt() != null) version.setEndOfLifeAt(request.getEndOfLifeAt());
        if (request.getIsImmutable() != null) version.setImmutable(request.getIsImmutable());
        if (request.getMetadata() != null) version.setMetadata(request.getMetadata());

        version.setUpdatedAt(Instant.now(clock));
        version.setUpdatedBy(userId);

        ProductVersion saved = productVersionRepository.save(version);
        return mapToResponseDto(saved);
    }

    @Override
    public void deleteProductVersion(String versionId) {
        ProductVersion version = productVersionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Product version not found with id: " + versionId));

        if (version.isImmutable()) {
            throw new BadRequestException("Cannot delete an immutable release snapshot version");
        }

        productVersionRepository.deleteById(versionId);
        log.info("Deleted product version {}", versionId);
    }

    // --- Helper Methods ---

    private Product findProductByIdOrSlug(String idOrSlug) {
        return productRepository.findById(idOrSlug)
                .or(() -> productRepository.findBySlug(idOrSlug))
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id or slug: " + idOrSlug));
    }

    private String resolveSlug(String slug, String versionName) {
        if (slug != null && !slug.isBlank()) {
            return slug.trim().toLowerCase().replaceAll("[^a-z0-9-_.]", "-");
        }
        return versionName.trim().toLowerCase().replaceAll("[^a-z0-9-_.]", "-");
    }

    private void validateVersionUniqueness(String productId, String versionName, String slug, String excludeId) {
        Optional<ProductVersion> byName = productVersionRepository.findByProductIdAndVersionName(productId, versionName.trim());
        if (byName.isPresent() && (excludeId == null || !byName.get().getId().equals(excludeId))) {
            throw new DuplicateResourceException("Product version name already exists: " + versionName);
        }

        Optional<ProductVersion> bySlug = productVersionRepository.findByProductIdAndSlug(productId, slug);
        if (bySlug.isPresent() && (excludeId == null || !bySlug.get().getId().equals(excludeId))) {
            throw new DuplicateResourceException("Product version slug already exists: " + slug);
        }
    }

    private void demoteOtherLatestVersions(String productId) {
        productVersionRepository.findByProductIdAndIsLatestTrue(productId).ifPresent(oldLatest -> {
            oldLatest.setLatest(false);
            productVersionRepository.save(oldLatest);
        });
    }

    private List<DocTreeNodeSnapshot> buildNavigationTree(List<Page> pages, String productSlug, String versionSlug) {
        if (pages == null || pages.isEmpty()) {
            return List.of();
        }

        Set<String> ids = pages.stream().map(Page::getId).collect(Collectors.toSet());
        Map<String, List<Page>> childrenByParent = new HashMap<>();
        List<Page> roots = new ArrayList<>();

        for (Page page : pages) {
            String parentId = page.getParentId();
            if (parentId == null || parentId.isBlank() || !ids.contains(parentId)) {
                roots.add(page);
            } else {
                childrenByParent.computeIfAbsent(parentId, key -> new ArrayList<>()).add(page);
            }
        }

        roots.sort(SIBLING_ORDER);
        childrenByParent.values().forEach(siblings -> siblings.sort(SIBLING_ORDER));

        Set<String> visiting = new HashSet<>();
        Set<String> placed = new HashSet<>();
        List<DocTreeNodeSnapshot> tree = new ArrayList<>();

        for (Page root : roots) {
            tree.add(toTreeNodeSnapshot(root, childrenByParent, visiting, placed, productSlug, versionSlug));
        }

        List<Page> leftover = new ArrayList<>();
        for (Page page : pages) {
            if (!placed.contains(page.getId())) {
                leftover.add(page);
            }
        }
        leftover.sort(SIBLING_ORDER);
        for (Page page : leftover) {
            if (!placed.contains(page.getId())) {
                tree.add(toTreeNodeSnapshot(page, childrenByParent, visiting, placed, productSlug, versionSlug));
            }
        }

        return tree;
    }

    private DocTreeNodeSnapshot toTreeNodeSnapshot(
            Page page,
            Map<String, List<Page>> childrenByParent,
            Set<String> visiting,
            Set<String> placed,
            String productSlug,
            String versionSlug) {

        placed.add(page.getId());

        String fullPath = "/products/" + productSlug + "/versions/" + versionSlug + "/pages/" + page.getSlug();

        DocTreeNodeSnapshot node = DocTreeNodeSnapshot.builder()
                .nodeId(page.getId())
                .pageId(page.getId())
                .parentId(page.getParentId())
                .title(page.getTitle())
                .slug(page.getSlug())
                .fullPath(fullPath)
                .nodeType(DocNodeType.PAGE)
                .displayOrder(page.getPageOrder())
                .isExpanded(false)
                .isHidden(false)
                .children(new ArrayList<>())
                .build();

        if (!visiting.add(page.getId())) {
            return node;
        }

        try {
            List<Page> children = childrenByParent.getOrDefault(page.getId(), List.of());
            for (Page child : children) {
                node.getChildren().add(toTreeNodeSnapshot(child, childrenByParent, visiting, placed, productSlug, versionSlug));
            }
        } finally {
            visiting.remove(page.getId());
        }

        return node;
    }

    private List<PageSnapshot> buildPageSnapshots(List<Page> pages, String productSlug, String versionSlug) {
        if (pages == null) return List.of();

        Instant now = Instant.now(clock);

        return pages.stream().map(page -> PageSnapshot.builder()
                .pageId(page.getId())
                .parentId(page.getParentId())
                .title(page.getTitle())
                .slug(page.getSlug())
                .fullPath("/products/" + productSlug + "/versions/" + versionSlug + "/pages/" + page.getSlug())
                .summary("")
                .pageOrder(page.getPageOrder())
                .blocks(page.getPublishedBlocks() != null ? new ArrayList<>(page.getPublishedBlocks()) : new ArrayList<>())
                .tableOfContents(new ArrayList<>())
                .metaTags(new ArrayList<>())
                .customAttributes(new HashMap<>())
                .lastModifiedAt(page.getUpdatedAt() != null ? page.getUpdatedAt() : page.getCreatedAt())
                .snapshottedAt(now)
                .build()
        ).collect(Collectors.toList());
    }

    private List<VersionResource> snapshotResources(List<ProductResource> resources) {
        if (resources == null) return new ArrayList<>();

        return resources.stream().map(res -> VersionResource.builder()
                .resourceId(res.getResourceId())
                .name(res.getName())
                .resourceType(res.getResourceType())
                .url(res.getUrl())
                .displayOrder(res.getDisplayOrder())
                .isActive(res.isActive())
                .build()
        ).collect(Collectors.toList());
    }

    private ProductVersionSummaryDto mapToSummaryDto(ProductVersion version) {
        return ProductVersionSummaryDto.builder()
                .id(version.getId())
                .productId(version.getProductId())
                .productSlug(version.getProductSlug())
                .versionName(version.getVersionName())
                .slug(version.getSlug())
                .displayTitle(version.getDisplayTitle())
                .status(version.getStatus())
                .isLatest(version.isLatest())
                .isDefault(version.isDefault())
                .isLts(version.isLts())
                .isDeprecated(version.isDeprecated())
                .deprecationNotice(version.getDeprecationNotice())
                .displayOrder(version.getDisplayOrder())
                .releasedAt(version.getReleasedAt())
                .endOfLifeAt(version.getEndOfLifeAt())
                .build();
    }

    private ProductVersionResponseDto mapToResponseDto(ProductVersion version) {
        return ProductVersionResponseDto.builder()
                .id(version.getId())
                .productId(version.getProductId())
                .productSlug(version.getProductSlug())
                .versionName(version.getVersionName())
                .slug(version.getSlug())
                .displayTitle(version.getDisplayTitle())
                .status(version.getStatus())
                .isLatest(version.isLatest())
                .isDefault(version.isDefault())
                .isLts(version.isLts())
                .isDeprecated(version.isDeprecated())
                .deprecationNotice(version.getDeprecationNotice())
                .displayOrder(version.getDisplayOrder())
                .releaseNotes(version.getReleaseNotes())
                .changelogUrl(version.getChangelogUrl())
                .apiSpecUrl(version.getApiSpecUrl())
                .releasedAt(version.getReleasedAt())
                .endOfLifeAt(version.getEndOfLifeAt())
                .navigationTree(version.getNavigationTree())
                .pageSnapshots(version.getPageSnapshots())
                .resources(version.getResources())
                .metadata(version.getMetadata())
                .isImmutable(version.isImmutable())
                .commitMessage(version.getCommitMessage())
                .snapshottedAt(version.getSnapshottedAt())
                .snapshottedBy(version.getSnapshottedBy())
                .createdAt(version.getCreatedAt())
                .updatedAt(version.getUpdatedAt())
                .createdBy(version.getCreatedBy())
                .updatedBy(version.getUpdatedBy())
                .build();
    }
}
