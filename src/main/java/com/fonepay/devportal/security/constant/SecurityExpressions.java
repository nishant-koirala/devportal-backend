package com.fonepay.devportal.security.constant;

public final class SecurityExpressions {

    private SecurityExpressions() {
    }

    public static final String HAS_ADMIN = "hasRole('ADMIN')";
    public static final String HAS_CMS = "hasAnyRole('ADMIN', 'EDITOR')";
    public static final String HAS_AUTHENTICATED = "isAuthenticated()";
}
