package com.fonepay.devportal.modules.auth.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fonepay.devportal.common.constant.enums.AuthStatus;

import lombok.Builder;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
public class AuthResponse {

    private String userId;
    private String fullName;
    private String email;
    private List<String> roles;
    private String token;
    private AuthStatus authStatus;
}