package com.fonepay.devportal.modules.cms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
import com.fonepay.devportal.modules.cms.enums.PageType;
import java.util.List;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePageRequest {

    private String sectionId;

    private String parentId;

    @NotNull(message = "Page type is required")
    private PageType type;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must be at most 200 characters")
    private String title;

    @NotBlank(message = "Slug is required")
    @Size(max = 120, message = "Slug must be at most 120 characters")
    @Pattern(
            regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
            message = "Slug must be lowercase alphanumeric words separated by hyphens")
    private String slug;

    @Valid
    private List<BlockDto> draftBlocks;
}
