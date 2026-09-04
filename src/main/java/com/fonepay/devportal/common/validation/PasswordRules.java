package com.fonepay.devportal.common.validation;

/**
 * Shared FR-103 password policy: min length, one upper-case letter, one digit,
 * and one non-alphanumeric character. Failure messages name the missing rule.
 */
public final class PasswordRules {

    public static final int DEFAULT_MIN_LENGTH = 8;

    private PasswordRules() {
    }

    /**
     * @return the first failed rule message, or {@code null} if the password is valid
     */
    public static String firstFailure(String password, int minLength) {
        if (password == null || password.length() < minLength) {
            return "Password must be at least " + minLength + " characters";
        }
        if (!containsUpperCase(password)) {
            return "Password must contain at least one upper-case letter";
        }
        if (!containsDigit(password)) {
            return "Password must contain at least one digit";
        }
        if (!containsSpecial(password)) {
            return "Password must contain at least one special character";
        }
        return null;
    }

    public static boolean isValid(String password, int minLength) {
        return firstFailure(password, minLength) == null;
    }

    private static boolean containsUpperCase(String password) {
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                return true;
            }
        }
        return false;
    }

    private static boolean containsDigit(String password) {
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (c >= '0' && c <= '9') {
                return true;
            }
        }
        return false;
    }

    private static boolean containsSpecial(String password) {
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            boolean letterOrDigit = (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9');
            if (!letterOrDigit) {
                return true;
            }
        }
        return false;
    }
}
