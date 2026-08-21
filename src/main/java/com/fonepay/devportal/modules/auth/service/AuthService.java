package com.fonepay.devportal.modules.auth.service;

import com.fonepay.devportal.modules.auth.dto.request.ForgotPasswordRequest;
import com.fonepay.devportal.modules.auth.dto.request.OtpVerifyRequest;
import com.fonepay.devportal.modules.auth.dto.request.ResetPasswordRequest;
import com.fonepay.devportal.modules.auth.dto.response.AuthResponse;
import com.fonepay.devportal.modules.auth.dto.response.OtpResponse;

public interface AuthService {

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    OtpResponse requestOtp(String pendingAuthId);

    AuthResponse verifyOtp(String pendingAuthId, OtpVerifyRequest request);
}
