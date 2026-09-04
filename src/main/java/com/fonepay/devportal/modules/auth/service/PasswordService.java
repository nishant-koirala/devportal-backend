package com.fonepay.devportal.modules.auth.service;

import com.fonepay.devportal.modules.auth.dto.request.ForgotPasswordRequest;
import com.fonepay.devportal.modules.auth.dto.request.ResetPasswordRequest;

public interface PasswordService {

    void forgotPassword(ForgotPasswordRequest request, String next);

    void resetPassword(ResetPasswordRequest request);
}
