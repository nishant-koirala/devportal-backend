package com.fonepay.devportal.modules.cms.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReorderPagesRequest {

    @NotEmpty(message = "At least one hierarchy update is required")
    @Valid
    private List<PageHierarchyUpdateDto> updates;
}
