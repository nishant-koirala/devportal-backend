package com.fonepay.devportal.common.constant.enums;

/**
 * Status of OTP verification.
 * Used to track the OTP verification state for a user.
 */
public enum OtpStatus {
    PENDING,    // OTP generated and sent, awaiting verification
    VERIFIED,   // OTP successfully verified
    EXPIRED,    // OTP expired
    FAILED,     // Max attempts exceeded
    NONE        // No OTP in progress
}
