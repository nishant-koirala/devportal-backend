package com.fonepay.devportal.modules.cms.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkPageSaveRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must be at most 200 characters")
    private String title;

    @NotBlank(message = "Slug is required")
    @Size(max = 120, message = "Slug must be at most 120 characters")
    @Pattern(
            regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
            message = "Slug must be lowercase alphanumeric words separated by hyphens")
    private String slug;

    @NotNull(message = "Version is required for optimistic locking")
    private Long version;

    @Valid
    private List<BlockDto> draftBlocks;

    private String commitMessage;

    @Size(max = 6, message = "A page can have at most 6 related pages")
    private List<String> relatedPageIds;
}
