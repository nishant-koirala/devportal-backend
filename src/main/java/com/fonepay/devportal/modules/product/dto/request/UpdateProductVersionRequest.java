package com.fonepay.devportal.modules.product.dto.request;

import java.time.Instant;
import java.util.Map;

import com.fonepay.devportal.modules.product.enums.ProductVersionStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductVersionRequest {

    private String displayTitle;

    private ProductVersionStatus status;

    private Boolean isLatest;

    private Boolean isDefault;

    private Boolean isLts;

    private Boolean isDeprecated;

    private String deprecationNotice;

    private Integer displayOrder;

    private String releaseNotes;

    private String changelogUrl;

    private String apiSpecUrl;

    private Instant releasedAt;

    private Instant endOfLifeAt;

    private Boolean isImmutable;

    private Map<String, Object> metadata;
}
