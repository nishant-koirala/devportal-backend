package com.fonepay.devportal.modules.cms.dto.request;

import com.fonepay.devportal.common.constant.PaginationConstants;
import com.fonepay.devportal.modules.cms.enums.ProductStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchCriteriaDto {

    @Builder.Default
    private int page = PaginationConstants.DEFAULT_PAGE_NUMBER;

    @Builder.Default
    private int size = PaginationConstants.DEFAULT_PAGE_SIZE;

    @Builder.Default
    private String sortDirection = PaginationConstants.DEFAULT_SORT_DIRECTION;

    @Builder.Default
    private String sortBy = "displayOrder";

    private ProductStatus status;

    private String search;
}
