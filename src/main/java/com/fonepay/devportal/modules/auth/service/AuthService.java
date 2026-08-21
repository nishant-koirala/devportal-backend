package com.fonepay.devportal.modules.auth.service;

import com.fonepay.devportal.modules.auth.dto.request.OtpVerifyRequest;
import com.fonepay.devportal.modules.auth.dto.response.AuthResponse;
import com.fonepay.devportal.modules.auth.dto.response.OtpResponse;

public interface AuthService {

    OtpResponse requestOtp(String tempToken);

    AuthResponse verifyOtp(String tempToken, OtpVerifyRequest request);
}
