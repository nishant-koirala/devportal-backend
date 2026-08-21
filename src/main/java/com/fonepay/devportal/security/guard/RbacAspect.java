package com.fonepay.devportal.security.guard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.fonepay.devportal.modules.auth.service.RbacService;
import com.fonepay.devportal.security.annotation.RequireAdmin;
import com.fonepay.devportal.security.annotation.RequireEditor;
import com.fonepay.devportal.security.annotation.RequireRole;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
@Order(1)
@RequiredArgsConstructor
public class RbacAspect {

    private final RbacService rbacService;

    @Around("@annotation(requireRole) || @within(requireRole)")
    public Object enforceRequireRole(ProceedingJoinPoint joinPoint, RequireRole requireRole) throws Throwable {
        RequireRole annotation = requireRole != null ? requireRole : resolveAnnotation(joinPoint, RequireRole.class);
        if (annotation != null && annotation.value().length > 0) {
            rbacService.validateCurrentUserRole(annotation.value());
        }
        return joinPoint.proceed();
    }

    @Around("@annotation(requireAdmin) || @within(requireAdmin)")
    public Object enforceRequireAdmin(ProceedingJoinPoint joinPoint, RequireAdmin requireAdmin) throws Throwable {
        rbacService.validateCurrentUserRole("ADMIN");
        return joinPoint.proceed();
    }

    @Around("@annotation(requireEditor) || @within(requireEditor)")
    public Object enforceRequireEditor(ProceedingJoinPoint joinPoint, RequireEditor requireEditor) throws Throwable {
        rbacService.validateCurrentUserRole("ADMIN", "EDITOR");
        return joinPoint.proceed();
    }

    @SuppressWarnings("unchecked")
    private <A extends java.lang.annotation.Annotation> A resolveAnnotation(ProceedingJoinPoint joinPoint, Class<A> annotationClass) {
        if (joinPoint.getSignature() instanceof MethodSignature methodSignature) {
            A annotation = methodSignature.getMethod().getAnnotation(annotationClass);
            if (annotation != null) {
                return annotation;
            }
        }
        return joinPoint.getTarget().getClass().getAnnotation(annotationClass);
    }
}
