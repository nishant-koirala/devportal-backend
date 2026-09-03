package com.fonepay.devportal.modules.product.document;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fonepay.devportal.modules.product.enums.DocNodeType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Snapshot of a documentation tree navigation node (as in angular.dev sidebar navigation).
 * Supports arbitrary hierarchical nesting of guides, sections, pages, and categories.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocTreeNodeSnapshot {

    @Field("node_id")
    private String nodeId;

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

    @Builder.Default
    @Field("node_type")
    private DocNodeType nodeType = DocNodeType.PAGE;

    @Field("display_order")
    private int displayOrder;

    @Field("icon")
    private String icon;

    @Field("badge")
    private String badge; // e.g., "NEW", "BETA", "EXPERIMENTAL", "DEPRECATED"

    @Field("external_url")
    private String externalUrl;

    @Builder.Default
    @Field("is_expanded")
    private boolean isExpanded = false;

    @Builder.Default
    @Field("is_hidden")
    private boolean isHidden = false;

    @Builder.Default
    @Field("children")
    private List<DocTreeNodeSnapshot> children = new ArrayList<>();
}
