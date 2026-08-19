package com.fonepay.devportal.modules.auth.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegistrationResponse {
    private String userId;
    private String email;
    private String fullName;
}
