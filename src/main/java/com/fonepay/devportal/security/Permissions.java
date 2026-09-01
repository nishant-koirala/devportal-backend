package com.fonepay.devportal.security;

public final class Permissions {

    private Permissions() {
        // Prevent instantiation
    }

    public static final String CMS_PAGE_CREATE = "CMS_PAGE_CREATE";
    public static final String CMS_PAGE_EDIT = "CMS_PAGE_EDIT";
    public static final String CMS_PAGE_SUBMIT = "CMS_PAGE_SUBMIT";
    public static final String CMS_PAGE_APPROVE = "CMS_PAGE_APPROVE";
    public static final String CMS_PAGE_PUBLISH = "CMS_PAGE_PUBLISH";

    public static final String USER_INVITE = "USER_INVITE";
    public static final String USER_MANAGE = "USER_MANAGE";

    public static final String PRODUCT_MANAGE = "PRODUCT_MANAGE";

    public static final String DOCUMENT_VIEW = "DOCUMENT_VIEW";
    
    public static final String SYSTEM_MANAGE = "SYSTEM_MANAGE";
}
