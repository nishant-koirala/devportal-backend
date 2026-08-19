package com.fonepay.devportal.modules.auth.service;

/**
 * Composite authentication facade interface combining segregated auth domain services.
 */
public interface AuthService extends 
        AuthenticationService, 
        UserRegistrationService, 
        PasswordService, 
        MfaAuthService {
}
