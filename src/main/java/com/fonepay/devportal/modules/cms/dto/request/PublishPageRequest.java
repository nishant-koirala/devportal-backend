package com.fonepay.devportal.modules.cms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublishPageRequest {

    @NotBlank(message = "Commit message is required")
    @Size(max = 255, message = "Commit message must be at most 255 characters")
    private String commitMessage;
}
