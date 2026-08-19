package com.fonepay.devportal.modules.auth.service;

/**
 * Composite authentication facade combining login, registration, password reset, and MFA.
 */
public interface AuthService extends
        AuthenticationService,
        UserRegistrationService,
        PasswordService,
        MfaAuthService {
}
