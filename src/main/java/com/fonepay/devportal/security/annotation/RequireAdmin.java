package com.fonepay.devportal.security.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.access.prepost.PreAuthorize;

import com.fonepay.devportal.security.constant.SecurityExpressions;

/**
 * Meta-annotation requiring ADMIN role using native Spring Method Security.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize(SecurityExpressions.HAS_ADMIN)
public @interface RequireAdmin {
}
