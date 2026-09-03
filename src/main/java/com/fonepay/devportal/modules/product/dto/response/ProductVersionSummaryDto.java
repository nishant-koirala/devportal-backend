package com.fonepay.devportal.modules.product.dto.response;

import java.time.Instant;

import com.fonepay.devportal.modules.product.enums.ProductVersionStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight DTO for version switcher dropdowns (as in angular.dev header).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVersionSummaryDto {

    private String id;
    private String productId;
    private String productSlug;
    private String versionName;
    private String slug;
    private String displayTitle;
    private ProductVersionStatus status;
    private boolean isLatest;
    private boolean isDefault;
    private boolean isLts;
    private boolean isDeprecated;
    private String deprecationNotice;
    private int displayOrder;
    private Instant releasedAt;
    private Instant endOfLifeAt;
}
