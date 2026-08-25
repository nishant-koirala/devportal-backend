package com.fonepay.devportal.modules.cms.dto.request;

import java.time.Instant;

import com.fonepay.devportal.common.constant.PaginationConstants;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogSearchCriteriaDto {

    @Builder.Default
    private int page = PaginationConstants.DEFAULT_PAGE_NUMBER;

    @Builder.Default
    private int size = PaginationConstants.DEFAULT_PAGE_SIZE;

    @Builder.Default
    private String sortDirection = "desc";

    @Builder.Default
    private String sortBy = "timestamp";

    private String adminId;

    private String targetId;

    private String targetType;

    private String action;

    private Instant startDate;

    private Instant endDate;
}
