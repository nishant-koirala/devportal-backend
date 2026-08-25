package com.fonepay.devportal.modules.cms.dto.response;

import java.time.Instant;

import com.fonepay.devportal.modules.cms.enums.ProductStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDto {
    private String id;
    private String name;
    private String slug;
    private String shortDescription;
    private String logoUrl;
    private ProductStatus status;
    private int displayOrder;
    private int resourceCount;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
}
