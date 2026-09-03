package com.fonepay.devportal.modules.product.document;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * On-page Table of Contents heading entry for a snapshotted documentation page.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocTocItem {

    @Field("id")
    private String id;

    @Field("title")
    private String title;

    @Field("level")
    private int level; // 1 for h1, 2 for h2, 3 for h3, etc.

    @Field("anchor")
    private String anchor; // URL anchor tag (e.g., "#getting-started")

    @Builder.Default
    @Field("children")
    private List<DocTocItem> children = new ArrayList<>();
}
