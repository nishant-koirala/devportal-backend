package com.fonepay.devportal.modules.cms.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageHierarchyUpdateDto {

    @NotBlank(message = "Page ID is required")
    private String pageId;

    private String parentId;

    @Min(value = 0, message = "Page order must be 0 or greater")
    private int pageOrder;
}
