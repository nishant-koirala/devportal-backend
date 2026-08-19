package com.fonepay.devportal.modules.auth.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OtpResponse {

    private String message;
    private int expiresInSeconds;
}