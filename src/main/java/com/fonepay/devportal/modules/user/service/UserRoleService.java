package com.fonepay.devportal.modules.user.service;

import java.util.List;

public interface UserRoleService {

    List<String> getRoleNamesByUserId(String userId);

    void assignDefaultRole(String userId, String roleName);
}
