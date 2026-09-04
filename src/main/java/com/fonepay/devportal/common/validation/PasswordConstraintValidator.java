package com.fonepay.devportal.common.validation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

@Component
public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {

    @Value("${app.password.min-length:" + PasswordRules.DEFAULT_MIN_LENGTH + "}")
    private int minLength = PasswordRules.DEFAULT_MIN_LENGTH;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        String failure = PasswordRules.firstFailure(value, minLength);
        if (failure == null) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(failure).addConstraintViolation();
        return false;
    }
}
