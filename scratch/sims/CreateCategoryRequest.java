package com.smartinventorysystem.modules.category.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCategoryRequest {

    @NotBlank(message = "Category name is required")
    private String categoryName;

    private String description;
}