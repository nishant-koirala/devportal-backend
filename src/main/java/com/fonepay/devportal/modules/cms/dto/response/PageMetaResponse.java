package com.fonepay.devportal.modules.cms.dto.response;

import java.time.Instant;

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
}
