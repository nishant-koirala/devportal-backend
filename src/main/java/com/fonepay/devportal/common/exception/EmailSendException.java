package com.fonepay.devportal.common.exception;

/** Thrown when SMTP send fails. Maps to HTTP 503 without leaking the cause. */
public class EmailSendException extends RuntimeException {
    public EmailSendException(String message) {
        super(message);
    }

    public EmailSendException(String message, Throwable cause) {
        super(message, cause);
    }
}
