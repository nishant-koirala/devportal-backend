package com.fonepay.devportal.modules.auth.service;

import com.fonepay.devportal.modules.auth.dto.reponse.AuthResponse;
import com.fonepay.devportal.modules.auth.dto.request.LoginRequest;

public interface AuthService {

    AuthResponse login(LoginRequest request, String ipAddress, String userAgent);

    void logout(String authHeader);
}
