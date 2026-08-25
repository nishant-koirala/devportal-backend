package com.fonepay.devportal.modules.cms.dto.request;

import com.fonepay.devportal.modules.cms.enums.ProductStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductStatusRequest {

    @NotNull(message = "Status is required")
    private ProductStatus status;
}
