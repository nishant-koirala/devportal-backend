package com.fonepay.devportal.modules.product.document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fonepay.devportal.modules.product.enums.ProductVersionStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MongoDB Document Model representing a major product release version snapshot
 * of documentation trees (inspired by angular.dev versioning architecture).
 *
 * It captures an immutable snapshot of the navigation hierarchy (sidebar tree),
 * all published page contents with their blocks, and developer assets for a specific
 * major/minor product release.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "product_versions")
@CompoundIndexes({
        @CompoundIndex(name = "idx_product_version_name", def = "{'product_id': 1, 'version_name': 1}", unique = true),
        @CompoundIndex(name = "idx_product_slug_version", def = "{'product_id': 1, 'slug': 1}", unique = true),
        @CompoundIndex(name = "idx_product_status_order", def = "{'product_id': 1, 'status': 1, 'display_order': 1}"),
        @CompoundIndex(name = "idx_product_is_latest", def = "{'product_id': 1, 'is_latest': 1}"),
        @CompoundIndex(name = "idx_prod_slug_ver_slug", def = "{'product_slug': 1, 'slug': 1}")
})
public class ProductVersion {

    @Id
    private String id;

    @Indexed
    @Field("product_id")
    private String productId;

    @Indexed
    @Field("product_slug")
    private String productSlug;

    @Field("version_name")
    private String versionName; // e.g., "v19.0.0", "v18.0.0", "v2.0"

    @Field("slug")
    private String slug; // e.g., "v19", "v18", "v2", "latest"

    @Field("display_title")
    private String displayTitle; // e.g., "v19 (Current)", "v18 (LTS)", "v17 (Deprecated)"

    @Builder.Default
    @Field("status")
    private ProductVersionStatus status = ProductVersionStatus.DRAFT;

    @Field("is_latest")
    private boolean isLatest;

    @Field("is_default")
    private boolean isDefault;

    @Field("is_lts")
    private boolean isLts;

    @Field("is_deprecated")
    private boolean isDeprecated;

    @Field("deprecation_notice")
    private String deprecationNotice; // Custom banner message displayed on top of docs

    @Field("display_order")
    private int displayOrder;

    @Field("release_notes")
    private String releaseNotes; // Markdown release highlights / overview

    @Field("changelog_url")
    private String changelogUrl; // Link to external release notes or git tag

    @Field("api_spec_url")
    private String apiSpecUrl; // Snapshot OpenAPI / Swagger spec URL

    @Field("released_at")
    private Instant releasedAt;

    @Field("end_of_life_at")
    private Instant endOfLifeAt;

    /**
     * Snapshot of the sidebar navigation hierarchy tree at the time of release.
     */
    @Builder.Default
    @Field("navigation_tree")
    private List<DocTreeNodeSnapshot> navigationTree = new ArrayList<>();

    /**
     * Deep snapshot of all published pages and block contents for this version.
     */
    @Builder.Default
    @Field("page_snapshots")
    private List<PageSnapshot> pageSnapshots = new ArrayList<>();

    /**
     * Snapshotted developer assets/resources (SDKs, Postman collections, etc.).
     */
    @Builder.Default
    @Field("resources")
    private List<VersionResource> resources = new ArrayList<>();

    /**
     * Flexible metadata attributes for custom extensions (e.g., framework version, npm package tag).
     */
    @Builder.Default
    @Field("metadata")
    private Map<String, Object> metadata = new HashMap<>();

    @Field("is_immutable")
    private boolean isImmutable;

    @Field("commit_message")
    private String commitMessage;

    @Field("snapshotted_at")
    private Instant snapshottedAt;

    @Field("snapshotted_by")
    private String snapshottedBy;

    @Field("created_at")
    private Instant createdAt;

    @Field("updated_at")
    private Instant updatedAt;

    @Field("created_by")
    private String createdBy;

    @Field("updated_by")
    private String updatedBy;

    @Version
    @Field("version")
    private Long version;
}
