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
        public static final String OTP_REQUEST = "/otp/request";
        public static final String OTP_VERIFY = "/otp/verify";

        public static final String ADMIN_LOGIN = "/admin/login";
        public static final String EDITOR_LOGIN = "/editor/login";
        public static final String ADMIN_OTP_SETUP = "/admin/otp/setup";
        public static final String ADMIN_OTP_VERIFY = "/admin/otp/verify";
        public static final String EDITOR_OTP_VERIFY = "/editor/otp/verify";
    }

    public static final class Admin {
        public static final String BASE = API_V1 + "/admin";
        public static final String DEVELOPERS = BASE + "/developers";
        public static final String DEVELOPER_BY_ID = "/{userId}";
        public static final String DEVELOPER_STATUS = "/{userId}/status";
        public static final String DEVELOPER_ACTIVITY = "/{userId}/activity";
        public static final String DEVELOPER_LOGIN_HISTORY = "/{userId}/login-history";
        public static final String PAGES = BASE + "/pages";
    }

    public static final class Cms {
        public static final String BASE = API_V1 + "/cms";
    }

    public static final class Department {
        public static final String BASE = API_V1 + "/departments";
    }
}
