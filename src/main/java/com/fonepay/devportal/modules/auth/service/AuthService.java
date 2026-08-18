package com.fonepay.devportal.modules.auth.service;

import com.fonepay.devportal.modules.auth.dto.reponse.AuthResponse;
import com.fonepay.devportal.modules.auth.dto.request.LoginRequest;

public interface AuthService {

    AuthResponse login(LoginRequest request, String ipAddress, String userAgent);

    void logout(String authHeader);

    com.fonepay.devportal.modules.auth.dto.response.RegistrationResponse register(com.fonepay.devportal.modules.auth.dto.request.RegisterRequest request);

    void verifyEmail(String token);

    void resendVerificationEmail(String email);
}
