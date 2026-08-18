package com.fonepay.devportal.modules.auth.service;

import com.fonepay.devportal.modules.auth.dto.reponse.AuthResponse;
import com.fonepay.devportal.modules.auth.dto.request.LoginRequest;
import com.fonepay.devportal.modules.auth.dto.request.OtpVerifyRequest;
import com.fonepay.devportal.modules.auth.dto.response.OtpResponse;

public interface AuthService {

    AuthResponse login(LoginRequest request, String ipAddress, String userAgent);

    void logout(String authHeader);

    OtpResponse requestOtp(String tempToken);

    AuthResponse verifyOtp(String tempToken, OtpVerifyRequest request);

    String extractUserIdFromToken(String token);
}