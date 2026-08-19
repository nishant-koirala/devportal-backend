package com.fonepay.devportal.modules.auth.dto.response;

import lombok.Builder;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fonepay.devportal.common.constant.enums.AuthStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
public class AuthResponse {

    private String userId;
    private String fullName;
    private String email;
    private java.util.List<String> roles;
    private String message;
    private String token;
    private AuthStatus authStatus;
}