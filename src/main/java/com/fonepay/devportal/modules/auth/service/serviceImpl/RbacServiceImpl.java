package com.fonepay.devportal.modules.auth.service.serviceImpl;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fonepay.devportal.common.exception.ForbiddenException;
import com.fonepay.devportal.common.exception.UnauthorizedException;
import com.fonepay.devportal.modules.auth.service.RbacService;
import com.fonepay.devportal.modules.user.service.UserRoleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RbacServiceImpl implements RbacService {

    private final UserRoleService userRoleService;

    @Override
    public boolean hasRole(String userId, String roleName) {
        if (userId == null || roleName == null) {
            return false;
        }
        List<String> roles = userRoleService.getRoleNamesByUserId(userId);
        return roles.stream().anyMatch(roleName::equalsIgnoreCase);
    }

    @Override
    public boolean hasAnyRole(String userId, List<String> roleNames) {
        if (userId == null || roleNames == null || roleNames.isEmpty()) {
            return false;
        }
        List<String> roles = userRoleService.getRoleNamesByUserId(userId);
        Set<String> targetRoles = roleNames.stream()
                .map(String::toUpperCase)
                .collect(Collectors.toSet());
        return roles.stream()
                .map(String::toUpperCase)
                .anyMatch(targetRoles::contains);
    }

    @Override
    public void checkRole(String userId, String requiredRole) {
        if (!hasRole(userId, requiredRole)) {
            log.warn("Access denied for user {}: Missing required role '{}'", userId, requiredRole);
            throw new ForbiddenException("Access denied: requires role '" + requiredRole + "'");
        }
    }

    @Override
    public void checkAnyRole(String userId, List<String> requiredRoles) {
        if (!hasAnyRole(userId, requiredRoles)) {
            log.warn("Access denied for user {}: Missing any required role in {}", userId, requiredRoles);
            throw new ForbiddenException("Access denied: requires one of the following roles: " + requiredRoles);
        }
    }

    @Override
    public List<String> getUserRoles(String userId) {
        return userRoleService.getRoleNamesByUserId(userId);
    }

    @Override
    public boolean isCurrentUserInRole(String roleName) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return false;
        }
        String targetAuthority = "ROLE_" + roleName.toUpperCase();
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(targetAuthority::equalsIgnoreCase);
    }

    @Override
    public boolean isCurrentUserInAnyRole(List<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            return false;
        }
        return roleNames.stream().anyMatch(this::isCurrentUserInRole);
    }

    @Override
    public void validateCurrentUserRole(String... requiredRoles) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new UnauthorizedException("Authentication required to access this resource");
        }

        if (requiredRoles == null || requiredRoles.length == 0) {
            return;
        }

        boolean authorized = Arrays.stream(requiredRoles).anyMatch(this::isCurrentUserInRole);
        if (!authorized) {
            log.warn("Current user denied access. Required roles: {}", Arrays.toString(requiredRoles));
            throw new ForbiddenException("Access denied: requires one of the following roles: " + Arrays.toString(requiredRoles));
        }
    }
}
