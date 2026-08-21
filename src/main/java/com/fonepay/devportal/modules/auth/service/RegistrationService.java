package com.fonepay.devportal.modules.auth.service;

import com.fonepay.devportal.modules.auth.dto.request.RegisterRequest;
import com.fonepay.devportal.modules.auth.dto.response.RegistrationResponse;

public interface RegistrationService {

    RegistrationResponse register(RegisterRequest request);

    void verifyEmail(String token);

    void resendVerificationEmail(String email);
}
