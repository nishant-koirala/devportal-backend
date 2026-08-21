package com.fonepay.devportal.common.constant.enums;

/**
 * Status of pending authentication (2FA) flow.
 * Used to track the OTP verification state for a pending authentication record.
 */
public enum PendingAuthStatus {
    PENDING,    // OTP generated and sent, awaiting verification
    VERIFIED,   // OTP successfully verified, session created
    EXPIRED,    // OTP expired
    FAILED      // Max attempts exceeded
}