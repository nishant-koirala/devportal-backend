package com.fonepay.devportal.modules.auth.policy;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.fonepay.devportal.modules.user.document.User;

@Component
public class RoleBasedMfaPolicy implements MfaPolicy {

    private static final Set<String> MFA_REQUIRED_ROLES = Set.of("ADMIN", "EDITOR");

    @Override
    public boolean isMfaRequired(User user, List<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            return false;
        }
        return roleNames.stream().anyMatch(MFA_REQUIRED_ROLES::contains);
    }
}
