package com.fonepay.devportal.modules.cms.dto.response;

import java.time.Instant;
import java.util.List;

import com.fonepay.devportal.modules.cms.document.Block;
import com.fonepay.devportal.modules.cms.enums.PageStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageMetaResponse {

    private String id;
    private Long version;
    private String productId;
    private String parentId;
    private int pageOrder;
    private String title;
    private String slug;
    private PageStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private Instant lastPublishedAt;
    private String reviewNotes;
    private String submittedBy;
    private Instant submittedAt;
    private String reviewedBy;
    private Instant reviewedAt;

    private List<Block> draftBlocks;
    private List<Block> publishedBlocks;
}
