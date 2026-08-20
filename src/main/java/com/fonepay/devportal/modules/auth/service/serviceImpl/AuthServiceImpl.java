package com.fonepay.devportal.modules.auth.service.serviceImpl;

import org.springframework.stereotype.Service;

import com.fonepay.devportal.modules.auth.dto.request.ForgotPasswordRequest;
import com.fonepay.devportal.modules.auth.dto.request.LoginRequest;
import com.fonepay.devportal.modules.auth.dto.request.OtpVerifyRequest;
import com.fonepay.devportal.modules.auth.dto.request.RegisterRequest;
import com.fonepay.devportal.modules.auth.dto.request.ResetPasswordRequest;
import com.fonepay.devportal.modules.auth.dto.response.AuthResponse;
import com.fonepay.devportal.modules.auth.dto.response.OtpResponse;
import com.fonepay.devportal.modules.auth.dto.response.RegistrationResponse;
import com.fonepay.devportal.modules.auth.service.AuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final LoginServiceImpl loginService;
    private final RegistrationServiceImpl registrationService;
    private final PasswordServiceImpl passwordService;
    private final MfaServiceImpl mfaService;

    @Override
    public AuthResponse login(LoginRequest request, String ipAddress, String userAgent) {
        return loginService.login(request, ipAddress, userAgent);
    }

    @Override
    public void logout(String authHeader) {
        loginService.logout(authHeader);
    }

    @Override
    public String extractUserIdFromToken(String token) {
        return loginService.extractUserIdFromToken(token);
    }

    @Override
    public RegistrationResponse register(RegisterRequest request) {
        return registrationService.register(request);
    }

    @Override
    public void verifyEmail(String token) {
        registrationService.verifyEmail(token);
    }

    @Override
    public void resendVerificationEmail(String email) {
        registrationService.resendVerificationEmail(email);
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        passwordService.forgotPassword(request);
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        passwordService.resetPassword(request);
    }

    @Override
    public OtpResponse requestOtp(String tempToken) {
        return mfaService.requestOtp(tempToken);
    }

    @Override
    public AuthResponse verifyOtp(String tempToken, OtpVerifyRequest request) {
        return mfaService.verifyOtp(tempToken, request);
    }
}
