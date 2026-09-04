package com.fonepay.devportal.common.constant;

public final class AuthMessages {

    public static final String INVALID_CREDENTIALS = "The email or password is incorrect.";
    public static final String DEACTIVATED =
            "This account has been deactivated. Contact developer@fonepay.com if you think this is a mistake.";
    public static final String SESSION_EXPIRED = "Your session has expired. Please log in again.";

    private AuthMessages() {
    }

    public static String unverified(String email) {
        return "Verify your email first. We sent a link to " + email + ".";
    }
}
