package com.fonepay.devportal.common.constant.enums;

/**
 * User roles for role-based access control.
 * ADMIN and EDITOR require mandatory OTP authentication.
 * DEVELOPER does not require OTP.
 */
public enum Role {
    DEVELOPER,
    ADMIN,
    EDITOR
}
