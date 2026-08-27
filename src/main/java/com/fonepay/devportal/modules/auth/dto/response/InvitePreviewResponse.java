package com.fonepay.devportal.modules.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitePreviewResponse {

    private String email;
    private String fullName;
    private String role;
    private String departmentId;
    private String departmentName;
}
