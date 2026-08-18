package com.fonepay.devportal.common.constant.apis;

public final class ApiRoutes {

    private ApiRoutes() {
    }

    public static final String API_V1 = "/api/v1";

    public static final class Auth {
        public static final String BASE = API_V1 + "/auth";
        public static final String LOGIN = "/login";
        public static final String LOGOUT = "/logout";
        public static final String OTP_REQUEST = "/otp/request";
        public static final String OTP_VERIFY = "/otp/verify";
    }

    public static final class Admin {
        public static final String BASE = API_V1 + "/admin";
    }

    public static final class Cms {
        public static final String BASE = API_V1 + "/cms";
    }

    public static final class Department {
        public static final String BASE = API_V1 + "/departments";
    }
}
