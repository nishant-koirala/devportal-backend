package com.fonepay.devportal.modules.auth.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RegistrationResponse {
    private String userId;
    private String email;
    private String fullName;
    private List<String> roles;
    private String next;
}
