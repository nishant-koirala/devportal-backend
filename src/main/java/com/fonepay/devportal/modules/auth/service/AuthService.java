package com.fonepay.devportal.modules.auth.service;

import com.fonepay.devportal.modules.auth.dto.response.AuthResponse;
import com.fonepay.devportal.modules.auth.dto.request.ForgotPasswordRequest;
import com.fonepay.devportal.modules.auth.dto.request.LoginRequest;
import com.fonepay.devportal.modules.auth.dto.request.OtpVerifyRequest;
import com.fonepay.devportal.modules.auth.dto.request.RegisterRequest;
import com.fonepay.devportal.modules.auth.dto.request.ResetPasswordRequest;
import com.fonepay.devportal.modules.auth.dto.response.OtpResponse;
import com.fonepay.devportal.modules.auth.dto.response.RegistrationResponse;

public interface AuthService {

    AuthResponse login(LoginRequest request, String ipAddress, String userAgent);

    void logout(String authHeader);

    RegistrationResponse register(RegisterRequest request);

    void verifyEmail(String token);

    void resendVerificationEmail(String email);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    OtpResponse requestOtp(String tempToken);

    AuthResponse verifyOtp(String tempToken, OtpVerifyRequest request);

    String extractUserIdFromToken(String token);
}
