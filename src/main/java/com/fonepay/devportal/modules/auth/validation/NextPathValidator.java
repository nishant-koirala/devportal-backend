package com.fonepay.devportal.modules.auth.validation;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;

/**
 * FR-110: only relative paths starting with {@code /} that are not {@code //}.
 * Absolute and protocol-relative values fall back to the dashboard.
 */
@Component
public class NextPathValidator {

    public static final String DASHBOARD = "/dashboard";

    public String resolve(String next) {
        if (next == null || next.isBlank()) {
            return DASHBOARD;
        }
        String trimmed = next.trim();
        if (!isSafeRelativePath(trimmed)) {
            return DASHBOARD;
        }
        return trimmed;
    }

    public String appendQuery(String url, String next) {
        if (url == null || url.isBlank() || next == null || next.isBlank()) {
            return url;
        }
        String encoded = URLEncoder.encode(resolve(next), StandardCharsets.UTF_8);
        return url + (url.contains("?") ? "&" : "?") + "next=" + encoded;
    }

    private boolean isSafeRelativePath(String path) {
        if (!path.startsWith("/") || path.startsWith("//")) {
            return false;
        }
        String lower = path.toLowerCase();
        if (lower.contains("http:") || lower.contains("https:") || lower.contains("://")) {
            return false;
        }
        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            if (c < 0x20 || c == '\\') {
                return false;
            }
        }
        return true;
    }
}
