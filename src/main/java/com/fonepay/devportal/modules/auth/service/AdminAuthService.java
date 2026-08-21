package com.fonepay.devportal.modules.auth.service;

import com.fonepay.devportal.modules.auth.dto.request.LoginRequest;
import com.fonepay.devportal.modules.auth.dto.request.OtpVerifyRequest;
import com.fonepay.devportal.modules.auth.dto.response.AuthResponse;
import com.fonepay.devportal.modules.auth.dto.response.OtpResponse;

public interface AdminAuthService {

    /**
     * Authenticate an Admin user.
     * Verifies credentials and ADMIN role, initiates OTP 2FA flow, and returns a pending auth ID.
     */
    AuthResponse adminLogin(LoginRequest request, String ipAddress, String userAgent);

    /**
     * Authenticate an Editor user.
     * Verifies credentials and EDITOR (or ADMIN) role, initiates OTP 2FA flow, and returns a pending auth ID.
     */
    AuthResponse editorLogin(LoginRequest request, String ipAddress, String userAgent);

    /**
     * Set up or resend an OTP for an active pending admin/editor authentication session.
     */
    OtpResponse setupOtp(String pendingAuthId);

    /**
     * Verify OTP code for Admin authentication.
     * Confirms ADMIN role, validates OTP, deletes pending auth, creates session, and returns JWT.
     */
    AuthResponse verifyAdminOtp(String pendingAuthId, OtpVerifyRequest request);

    /**
     * Verify OTP code for Editor authentication.
     * Confirms EDITOR or ADMIN role, validates OTP, deletes pending auth, creates session, and returns JWT.
     */
    AuthResponse verifyEditorOtp(String pendingAuthId, OtpVerifyRequest request);
}
