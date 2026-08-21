package com.fonepay.devportal.modules.auth.service.serviceImpl;

import org.springframework.stereotype.Service;

import com.fonepay.devportal.modules.auth.dto.request.ForgotPasswordRequest;
import com.fonepay.devportal.modules.auth.dto.request.OtpVerifyRequest;
import com.fonepay.devportal.modules.auth.dto.request.ResetPasswordRequest;
import com.fonepay.devportal.modules.auth.dto.response.AuthResponse;
import com.fonepay.devportal.modules.auth.dto.response.OtpResponse;
import com.fonepay.devportal.modules.auth.service.AuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final PasswordServiceImpl passwordService;
    private final MfaServiceImpl mfaService;

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        passwordService.forgotPassword(request);
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        passwordService.resetPassword(request);
    }

    @Override
    public OtpResponse requestOtp(String pendingAuthId) {
        return mfaService.requestOtp(pendingAuthId);
    }

    @Override
    public AuthResponse verifyOtp(String pendingAuthId, OtpVerifyRequest request) {
        return mfaService.verifyOtp(pendingAuthId, request);
    }
}
