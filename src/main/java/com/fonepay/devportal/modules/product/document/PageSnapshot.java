package com.fonepay.devportal.modules.product.document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fonepay.devportal.modules.cms.document.Block;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Immutable content snapshot of a published documentation page at the time of a major release.
 * Preserves the full block structure, heading hierarchy, and meta information for historical viewing.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageSnapshot {

    @Field("page_id")
    private String pageId;

    @Field("parent_id")
    private String parentId;

    @Field("title")
    private String title;

    @Field("slug")
    private String slug;

    @Field("full_path")
    private String fullPath;

    @Field("summary")
    private String summary;

    @Field("page_order")
    private int pageOrder;

    @Builder.Default
    @Field("blocks")
    private List<Block> blocks = new ArrayList<>();

    @Builder.Default
    @Field("table_of_contents")
    private List<DocTocItem> tableOfContents = new ArrayList<>();

    @Builder.Default
    @Field("meta_tags")
    private List<String> metaTags = new ArrayList<>();

    @Builder.Default
    @Field("custom_attributes")
    private Map<String, Object> customAttributes = new HashMap<>();

    @Field("last_modified_at")
    private Instant lastModifiedAt;

    @Field("snapshotted_at")
    private Instant snapshottedAt;
}
