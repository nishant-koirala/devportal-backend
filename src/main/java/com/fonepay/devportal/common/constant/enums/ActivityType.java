package com.fonepay.devportal.common.constant.enums;

/**
 * Labels for developer activity rows shown to admin.
 * LOGIN / LOGOUT / EMAIL_VERIFIED / PASSWORD_RESET are recorded now.
 * The rest are reserved for later modules (profile, product, logged-in password change).
 */
public enum ActivityType {
    LOGIN,
    LOGOUT,
    PASSWORD_CHANGED,
    PROFILE_UPDATED,
    PRODUCT_ADDED,
    PRODUCT_REMOVED,
    EMAIL_VERIFIED,
    PASSWORD_RESET
}