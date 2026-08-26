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
public class RejectPageRequest {

    @NotBlank(message = "Rejection reason / review notes are required")
    @Size(max = 2000, message = "Reason must not exceed 2000 characters")
    private String reason;
}
