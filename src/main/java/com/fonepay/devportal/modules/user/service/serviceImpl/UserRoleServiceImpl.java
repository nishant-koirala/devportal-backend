package com.fonepay.devportal.modules.user.service;

import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fonepay.devportal.common.exception.ResourceNotFoundException;
import com.fonepay.devportal.modules.user.document.AssignedRole;
import com.fonepay.devportal.modules.user.document.Role;
import com.fonepay.devportal.modules.user.document.User;
import com.fonepay.devportal.modules.user.repository.RoleRepository;
import com.fonepay.devportal.modules.user.repository.UserRepository;

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
    public List<String> getRoleNamesByUserId(String userId) {
        return userRepository.findById(userId)
                .map(User::getRoles)
                .map(roles -> roles.stream()
                        .map(AssignedRole::getRoleName)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    @Override
    public void assignDefaultRole(String userId, String roleName) {
        // Ensure the role exists in the master Role collection
        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException(roleName + " role not found in database"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        boolean hasRole = user.getRoles().stream()
                .anyMatch(r -> r.getRoleName().equals(role.getRoleName()));

        if (!hasRole) {
            AssignedRole newRole = AssignedRole.builder()
                    .roleName(role.getRoleName())
                    .assignedAt(Instant.now(clock))
                    .assignedBy("SYSTEM") // Can be updated if an Admin assigns this later
                    .build();

            user.getRoles().add(newRole);
            userRepository.save(user);
            log.info("Assigned role '{}' to user '{}'", roleName, userId);
        } else {
            log.info("User '{}' already has role '{}'", userId, roleName);
        }
    }
}
