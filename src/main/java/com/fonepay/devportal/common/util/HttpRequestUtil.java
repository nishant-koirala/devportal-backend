package com.fonepay.devportal.common.util;

import jakarta.servlet.http.HttpServletRequest;

public final class HttpRequestUtil {

    private HttpRequestUtil() {
    }

    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || "unknown".equalsIgnoreCase(xfHeader)) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }

    public static String getUserAgent(HttpServletRequest request) {
        return request != null ? request.getHeader("User-Agent") : null;
    }
}
