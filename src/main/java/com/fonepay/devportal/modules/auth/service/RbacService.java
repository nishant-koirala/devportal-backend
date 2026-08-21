package com.fonepay.devportal.modules.auth.service;

import java.util.List;

public interface RbacService {

    /**
     * Check if a specific user has a given role.
     */
    boolean hasRole(String userId, String roleName);

    /**
     * Check if a specific user has any of the given roles.
     */
    boolean hasAnyRole(String userId, List<String> roleNames);

    /**
     * Assert that a user has a specific role, throwing ForbiddenException if not.
     */
    void checkRole(String userId, String requiredRole);

    /**
     * Assert that a user has at least one of the given roles, throwing ForbiddenException if not.
     */
    void checkAnyRole(String userId, List<String> requiredRoles);

    /**
     * Retrieve all role names assigned to a user.
     */
    List<String> getUserRoles(String userId);

    /**
     * Check if the currently authenticated user in SecurityContext has the given role.
     */
    boolean isCurrentUserInRole(String roleName);

    /**
     * Check if the currently authenticated user in SecurityContext has any of the given roles.
     */
    boolean isCurrentUserInAnyRole(List<String> roleNames);

    /**
     * Validate that the currently authenticated user has at least one of the required roles.
     * Throws UnauthorizedException if not authenticated, or ForbiddenException if unauthorized.
     */
    void validateCurrentUserRole(String... requiredRoles);
}
