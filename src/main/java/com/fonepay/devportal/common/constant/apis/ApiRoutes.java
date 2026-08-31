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
        public static final String ACCEPT_INVITE = "/accept-invite";
    }

    public static final class Admin {
        public static final String BASE = API_V1 + "/admin";
        public static final String DEVELOPERS = BASE + "/developers";
        public static final String DEVELOPER_BY_ID = "/{userId}";
        public static final String DEVELOPER_STATUS = "/{userId}/status";
        public static final String DEVELOPER_ACTIVITY = "/{userId}/activity";
        public static final String DEVELOPER_LOGIN_HISTORY = "/{userId}/login-history";

        public static final String PRODUCTS = BASE + "/products";
        public static final String PRODUCT_BY_ID = "/{id}";
        public static final String PRODUCT_STATUS = "/{id}/status";
        public static final String PRODUCT_SUBMIT_REVIEW = "/{id}/submit-review";
        public static final String PRODUCT_APPROVE = "/{id}/approve";
        public static final String PRODUCT_REJECT = "/{id}/reject";
        public static final String PRODUCT_RESOURCES = "/{id}/resources";
        public static final String PRODUCT_RESOURCE_BY_ID = "/{id}/resources/{resourceId}";

        public static final String AUDIT_LOGS = BASE + "/audit-logs";

        public static final String DEPARTMENTS = BASE + "/departments";
        public static final String INVITATIONS = BASE + "/invitations";
        public static final String BROADCASTS = BASE + "/broadcasts";
        public static final String BROADCAST_BY_ID = "/{id}";
        public static final String BROADCAST_CANCEL = "/{id}/cancel";
        public static final String BROADCAST_METRICS = "/{id}/metrics";
    }

    public static final class Staff {
        public static final String BASE = API_V1 + "/staff";
        public static final String BROADCASTS = BASE + "/broadcasts";
        public static final String BROADCAST_STREAM = "/stream";
        public static final String BROADCAST_ACTIVE = "/active";
        public static final String BROADCAST_SUMMARY = "/summary";
        public static final String BROADCAST_READ = "/{id}/read";
        public static final String BROADCAST_DISMISS = "/{id}/dismiss";
        public static final String BROADCAST_READ_ALL = "/read-all";
    }

    public static final class Public {
        public static final String BASE = API_V1 + "/public";
        public static final String PRODUCTS = BASE + "/products";
        public static final String PRODUCT_BY_SLUG = "/{slug}";
        public static final String PRODUCT_PAGE = "/{productSlug}/pages/{pageSlug}";
        public static final String PAGES = BASE + "/pages";
    }

    public static final class Cms {
        public static final String BASE = API_V1 + "/cms";
        public static final String PRODUCT_PAGES = "/products/{productId}/pages";
        public static final String PRODUCT_PAGE_TREE = "/products/{productId}/pages/tree";
        public static final String PRODUCT_PAGES_REORDER = "/products/{productId}/pages/reorder";
        public static final String PAGE_BY_ID = "/pages/{pageId}";
        public static final String PAGE_SUBMIT_REVIEW = "/pages/{pageId}/submit-review";
        public static final String PAGE_APPROVE = "/pages/{pageId}/approve";
        public static final String PAGE_REJECT = "/pages/{pageId}/reject";
        public static final String PAGE_PUBLISH = "/pages/{pageId}/publish";
        public static final String PAGE_VERSIONS = "/pages/{pageId}/versions";
        public static final String PAGE_VERSION_BY_NUMBER = "/pages/{pageId}/versions/{versionNumber}";
        public static final String PAGE_REVERT = "/pages/{pageId}/revert/{versionNumber}";
    }

    public static final class Department {
        public static final String BASE = API_V1 + "/departments";
    }

    public static final class Profile {
        public static final String BASE = API_V1 + "/profile";
        public static final String PASSWORD = "/password";
        public static final String EMAIL_REQUEST = "/email/request";
        public static final String EMAIL_VERIFY = "/email/verify";
    }
}
