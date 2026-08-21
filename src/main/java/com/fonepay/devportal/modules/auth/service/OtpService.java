package com.fonepay.devportal.modules.auth.service;

public interface OtpService {

    /**
     * Generate a new 6-digit OTP code (plain text, for sending via email).
     */
    String generateOtpCode();

    /**
     * Hash an OTP code using SHA-256 for secure storage.
     */
    String hashOtp(String otpCode);

    /**
     * Verify the provided OTP code against the stored hash.
     */
    boolean verifyOtpCode(String providedCode, String storedHash);
}
