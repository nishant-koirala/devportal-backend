package com.fonepay.devportal.modules.cms.document;

import com.fonepay.devportal.modules.cms.enums.PageStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "pages")
@CompoundIndexes({
        @CompoundIndex(name = "product_slug_idx", def = "{'product_id': 1, 'slug': 1}", unique = true),
        @CompoundIndex(name = "product_parent_order_idx", def = "{'product_id': 1, 'parent_id': 1, 'page_order': 1}")
})
public class Page {

    @Id
    private String id;

    @Indexed
    @Field("product_id")
    private String productId;

    @Indexed
    @Field("parent_id")
    private String parentId;

    @Field("page_order")
    private int pageOrder;

    @Field("title")
    private String title;

    @Field("slug")
    private String slug;

    @Field("status")
    private PageStatus status;

    @Field("draft_blocks")
    private List<Block> draftBlocks = new ArrayList<>();

    @Field("published_blocks")
    private List<Block> publishedBlocks = new ArrayList<>();

    @Field("created_at")
    private Instant createdAt;

    @Field("updated_at")
    private Instant updatedAt;

    @Field("created_by")
    private String createdBy;

    @Field("last_published_at")
    private Instant lastPublishedAt;

    @Field("review_notes")
    private String reviewNotes;

    @Field("submitted_by")
    private String submittedBy;

    @Field("submitted_at")
    private Instant submittedAt;

    @Field("reviewed_by")
    private String reviewedBy;

    @Field("reviewed_at")
    private Instant reviewedAt;
}
