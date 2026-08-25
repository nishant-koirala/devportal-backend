package com.fonepay.devportal.modules.cms.dto.response;

import java.time.Instant;
import java.util.List;

import com.fonepay.devportal.modules.cms.enums.ProductStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailResponseDto {
    private String id;
    private String name;
    private String slug;
    private String shortDescription;
    private String description;
    private String logoUrl;
    private ProductStatus status;
    private int displayOrder;
    private List<ProductResourceResponseDto> resources;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
}
