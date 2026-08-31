package com.fonepay.devportal.modules.user.service;

import java.util.List;

public interface UserRoleService {

    List<String> getRoleNamesByUserId(String userId);

    void assignDefaultRole(String userId, String roleName);

    void assignRole(String userId, String roleName, String assignedBy);

    /**
     * Removes existing ADMIN/EDITOR assignments and sets exactly one staff role.
     */
    void replaceStaffRole(String userId, String roleName, String assignedBy);

    java.util.Set<String> getPermissionsByUserId(String userId);
}
