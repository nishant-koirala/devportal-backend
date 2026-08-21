package com.fonepay.devportal.modules.auth.service.serviceImpl;

import org.springframework.stereotype.Service;

import com.fonepay.devportal.modules.auth.dto.request.OtpVerifyRequest;
import com.fonepay.devportal.modules.auth.dto.response.AuthResponse;
import com.fonepay.devportal.modules.auth.dto.response.OtpResponse;
import com.fonepay.devportal.modules.auth.service.AuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final MfaServiceImpl mfaService;

    @Override
    public OtpResponse requestOtp(String tempToken) {
        return mfaService.requestOtp(tempToken);
    }

    @Override
    public AuthResponse verifyOtp(String tempToken, OtpVerifyRequest request) {
        return mfaService.verifyOtp(tempToken, request);
    }
}
