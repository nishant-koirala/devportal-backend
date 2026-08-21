package com.fonepay.devportal.modules.admin.developer.dto.request;

import com.fonepay.devportal.common.constant.PaginationConstants;
import com.fonepay.devportal.common.constant.enums.UserStatus;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeveloperSearchCriteriaDto {

    @Builder.Default
    @Min(value = 0, message = "Page number must be 0 or greater")
    private Integer page = PaginationConstants.DEFAULT_PAGE_NUMBER;

    @Builder.Default
    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = PaginationConstants.MAX_PAGE_SIZE, message = "Page size cannot exceed " + PaginationConstants.MAX_PAGE_SIZE)
    private Integer size = PaginationConstants.DEFAULT_PAGE_SIZE;

    private String search;

    private UserStatus status;

    @Builder.Default
    private String sortBy = PaginationConstants.DEFAULT_SORT_BY;

    @Builder.Default
    private String sortDirection = PaginationConstants.DEFAULT_SORT_DIRECTION;

    public int getPage() {
        return page != null && page >= 0 ? page : PaginationConstants.DEFAULT_PAGE_NUMBER;
    }

    public int getSize() {
        if (size == null || size < 1) {
            return PaginationConstants.DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, PaginationConstants.MAX_PAGE_SIZE);
    }

    public String getSortBy() {
        return (sortBy != null && !sortBy.isBlank()) ? sortBy.trim() : PaginationConstants.DEFAULT_SORT_BY;
    }

    public String getSortDirection() {
        return (sortDirection != null && !sortDirection.isBlank()) ? sortDirection.trim().toUpperCase() : PaginationConstants.DEFAULT_SORT_DIRECTION;
    }

    public String getSearch() {
        return (search != null && !search.isBlank()) ? search.trim() : null;
    }
}
