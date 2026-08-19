package com.fonepay.devportal.modules.auth.service;

import com.fonepay.devportal.modules.auth.dto.request.LoginRequest;
import com.fonepay.devportal.modules.auth.dto.response.AuthResponse;

public interface AuthenticationService {

    AuthResponse login(LoginRequest request, String ipAddress, String userAgent);

    void logout(String authHeader);

    String extractUserIdFromToken(String token);
}
