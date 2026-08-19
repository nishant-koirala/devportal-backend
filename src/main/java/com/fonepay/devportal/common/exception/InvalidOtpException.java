package com.fonepay.devportal.common.exception;

/** Thrown when the OTP code is wrong, expired, or locked. Maps to HTTP 400. */
public class InvalidOtpException extends RuntimeException {
    public InvalidOtpException(String message) {
        super(message);
    }
}
