package com.fonepay.devportal.modules.user.service.serviceImpl;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fonepay.devportal.common.exception.ResourceNotFoundException;
import com.fonepay.devportal.common.util.IdGenerator;
import com.fonepay.devportal.modules.user.document.Role;
import com.fonepay.devportal.modules.user.document.User;
import com.fonepay.devportal.modules.user.document.UserRole;
import com.fonepay.devportal.modules.user.repository.RoleRepository;
import com.fonepay.devportal.modules.user.repository.UserRepository;
import com.fonepay.devportal.modules.user.service.UserRoleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRoleServiceImpl implements UserRoleService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public List<String> getRoleNamesByUserId(String userId) {
        return userRepository.findById(userId)
                .map(User::getRoles)
                .map(roles -> roles.stream()
                        .map(UserRole::getRoleName)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    @Override
    @Transactional
    public void assignDefaultRole(String userId, String roleName) {
        assignRole(userId, roleName, "SYSTEM");
    }

    @Override
    @Transactional
    public void assignRole(String userId, String roleName, String assignedBy) {
        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException(
                        roleName + " role not found in database"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        if (user.getRoles() == null) {
            user.setRoles(new ArrayList<>());
        }

        boolean hasRole = user.getRoles().stream()
                .anyMatch(r -> role.getRoleName().equals(r.getRoleName()));

        if (!hasRole) {
            user.getRoles().add(UserRole.builder()
                    .id(IdGenerator.nextUlid())
                    .user(user)
                    .role(role)
                    .assignedAt(Instant.now(clock))
                    .assignedBy(assignedBy != null && !assignedBy.isBlank() ? assignedBy : "SYSTEM")
                    .build());
            userRepository.save(user);
            log.info("Assigned role '{}' to user '{}' by '{}'", roleName, userId, assignedBy);
        } else {
            log.info("User '{}' already has role '{}'", userId, roleName);
        }
    }

    @Override
    @Transactional
    public void replaceStaffRole(String userId, String roleName, String assignedBy) {
        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException(
                        roleName + " role not found in database"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        if (user.getRoles() == null) {
            user.setRoles(new ArrayList<>());
        }

        user.getRoles().removeIf(r -> "ADMIN".equalsIgnoreCase(r.getRoleName())
                || "EDITOR".equalsIgnoreCase(r.getRoleName()));

        user.getRoles().add(UserRole.builder()
                .id(IdGenerator.nextUlid())
                .user(user)
                .role(role)
                .assignedAt(Instant.now(clock))
                .assignedBy(assignedBy != null && !assignedBy.isBlank() ? assignedBy : "SYSTEM")
                .build());

        userRepository.save(user);
        log.info("Replaced staff role with '{}' for user '{}' by '{}'", roleName, userId, assignedBy);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<String> getPermissionsByUserId(String userId) {
        List<String> roleNames = getRoleNamesByUserId(userId);
        if (roleNames.isEmpty()) {
            return Collections.emptySet();
        }

        Set<String> allPermissions = new HashSet<>();
        for (String name : roleNames) {
            roleRepository.findByRoleName(name).ifPresent(role -> {
                if (role.getPermissions() != null) {
                    allPermissions.addAll(role.getPermissions());
                }
            });
        }
        return allPermissions;
    }
}
