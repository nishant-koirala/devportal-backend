package com.fonepay.devportal.common.constant.apis;

public final class ApiRoutes {

    private ApiRoutes() {
    }

    public static final String API_V1 = "/api/v1";

    public static final class Auth {
        public static final String BASE = API_V1 + "/auth";
        public static final String LOGIN = "/login";
        public static final String LOGOUT = "/logout";
        public static final String REGISTER = "/register";
        public static final String VERIFY_EMAIL = "/verify-email";
        public static final String RESEND_VERIFICATION = "/resend-verification";
        public static final String FORGOT_PASSWORD = "/forgot-password";
        public static final String RESET_PASSWORD = "/reset-password";
    }

    public static final class Department {
        public static final String BASE = API_V1 + "/departments";
    }
}
