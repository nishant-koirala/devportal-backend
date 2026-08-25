package com.fonepay.devportal.modules.cms.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicProductResponseDto {
    private String id;
    private String name;
    private String slug;
    private String shortDescription;
    private String description;
    private String logoUrl;
    private int displayOrder;
    private List<ProductResourceResponseDto> resources;
}
