package com.fonepay.devportal.modules.developer.constant;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class SdkConstants {

    private SdkConstants() {
    }

    public static final String LANG_JAVA = "java";
    public static final String LANG_PHP = "php";
    public static final String LANG_PYTHON = "python";
    public static final String LANG_JAVASCRIPT = "javascript";

    private static final Map<String, String> SDK_REPOSITORY_URLS;

    static {
        Map<String, String> urls = new HashMap<>();
        urls.put(LANG_JAVA, "https://github.com/fonepay/fonepay-sdk-java");
        urls.put(LANG_PHP, "https://github.com/fonepay/fonepay-sdk-php");
        urls.put(LANG_PYTHON, "https://github.com/fonepay/fonepay-sdk-python");
        urls.put(LANG_JAVASCRIPT, "https://github.com/fonepay/fonepay-sdk-js");
        // Alias for JS
        urls.put("js", "https://github.com/fonepay/fonepay-sdk-js");
        SDK_REPOSITORY_URLS = Collections.unmodifiableMap(urls);
    }

    public static final Set<String> SUPPORTED_LANGUAGES = Set.of(
            LANG_JAVA,
            LANG_PHP,
            LANG_PYTHON,
            LANG_JAVASCRIPT
    );

    public static boolean isSupported(String language) {
        if (language == null || language.isBlank()) {
            return false;
        }
        String normalized = normalizeLanguage(language);
        return SDK_REPOSITORY_URLS.containsKey(normalized);
    }

    public static String getRepositoryUrl(String language) {
        if (language == null || language.isBlank()) {
            return null;
        }
        return SDK_REPOSITORY_URLS.get(normalizeLanguage(language));
    }

    public static String normalizeLanguage(String language) {
        return language.trim().toLowerCase();
    }
}
