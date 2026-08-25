package com.fonepay.devportal.modules.cms.dto.request;

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
public class CreateProductResourceRequest {

    @NotBlank(message = "Resource name is required")
    @Size(min = 2, max = 100, message = "Resource name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Resource type is required")
    @Size(max = 50, message = "Resource type must not exceed 50 characters")
    private String resourceType;

    @NotBlank(message = "Resource URL is required")
    @Pattern(regexp = "^https?://.*$", message = "Resource URL must be a valid HTTP/HTTPS URL")
    private String url;

    @Min(value = 0, message = "Display order must be a non-negative integer")
    private int displayOrder;

    @Builder.Default
    private boolean isActive = true;

    public boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }
}


