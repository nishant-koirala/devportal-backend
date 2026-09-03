package com.fonepay.devportal.modules.product.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.fonepay.devportal.modules.product.document.DocTreeNodeSnapshot;
import com.fonepay.devportal.modules.product.document.PageSnapshot;
import com.fonepay.devportal.modules.product.document.VersionResource;
import com.fonepay.devportal.modules.product.enums.ProductVersionStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVersionResponseDto {

    private String id;
    private String productId;
    private String productSlug;
    private String versionName;
    private String slug;
    private String displayTitle;
    private ProductVersionStatus status;
    private boolean isLatest;
    private boolean isDefault;
    private boolean isLts;
    private boolean isDeprecated;
    private String deprecationNotice;
    private int displayOrder;
    private String releaseNotes;
    private String changelogUrl;
    private String apiSpecUrl;
    private Instant releasedAt;
    private Instant endOfLifeAt;
    private List<DocTreeNodeSnapshot> navigationTree;
    private List<PageSnapshot> pageSnapshots;
    private List<VersionResource> resources;
    private Map<String, Object> metadata;
    private boolean isImmutable;
    private String commitMessage;
    private Instant snapshottedAt;
    private String snapshottedBy;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
