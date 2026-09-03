package com.fonepay.devportal.modules.product.dto.request;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.fonepay.devportal.modules.product.document.DocTreeNodeSnapshot;
import com.fonepay.devportal.modules.product.document.PageSnapshot;
import com.fonepay.devportal.modules.product.document.VersionResource;
import com.fonepay.devportal.modules.product.enums.ProductVersionStatus;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductVersionRequest {

    @NotBlank(message = "Version name is required (e.g., v18.0.0, v2.0)")
    private String versionName;

    private String slug; // If blank, derived from versionName (e.g., "v18", "v2")

    private String displayTitle; // e.g., "v18 (Latest)", "v17 (LTS)"

    @Builder.Default
    private ProductVersionStatus status = ProductVersionStatus.ACTIVE;

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

    private String commitMessage;

    private boolean isImmutable;

    // Optional manual snapshot payload if not snapshotting live pages
    private List<DocTreeNodeSnapshot> navigationTree;

    private List<PageSnapshot> pageSnapshots;

    private List<VersionResource> resources;

    private Map<String, Object> metadata;
}
