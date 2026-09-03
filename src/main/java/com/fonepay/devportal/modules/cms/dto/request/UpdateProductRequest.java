package com.fonepay.devportal.modules.cms.dto.request;

import com.fonepay.devportal.modules.cms.enums.ProductStatus;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Product slug is required")
    @Size(min = 2, max = 100, message = "Product slug must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "Slug must contain only lowercase letters, numbers, and hyphens (e.g. 'payment-gateway')")
    private String slug;

    @Size(max = 255, message = "Short description must not exceed 255 characters")
    private String shortDescription;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @Pattern(regexp = "^(https?://.*)?$", message = "Logo URL must be a valid HTTP/HTTPS URL")
    private String logoUrl;

    private ProductStatus status;

    @Min(value = 0, message = "Display order must be a non-negative integer")
    private Integer displayOrder;

    private Long version;
}
