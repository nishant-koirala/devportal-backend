package com.fonepay.devportal.modules.auth.service;

import com.fonepay.devportal.modules.user.document.User;

public interface OtpService {

    /** Create a 6-digit OTP and save it on the user. */
    String generateOtp(User user);

    /** Return true if the code matches and is still valid. */
    boolean verifyOtp(User user, String providedCode);

    /** True if an OTP is pending and not expired. */
    boolean hasPendingOtp(User user);

    /** Remove OTP fields from the user. */
    void clearOtp(User user);

    /** Seconds left until expiry, or 0. */
    long getOtpRemainingSeconds(User user);

    int getMaxAttempts();
}
