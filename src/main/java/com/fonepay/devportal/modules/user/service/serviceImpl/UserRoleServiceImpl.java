package com.fonepay.devportal.modules.user.service.serviceImpl;

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
                assignRole(userId, roleName, "SYSTEM");
        }

        @Override
        public void assignRole(String userId, String roleName, String assignedBy) {
                Role role = roleRepository.findByRoleName(roleName)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                roleName + " role not found in database"));

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

                if (user.getRoles() == null) {
                        user.setRoles(new java.util.ArrayList<>());
                }

                boolean hasRole = user.getRoles().stream()
                                .anyMatch(r -> r.getRoleName().equals(role.getRoleName()));

                if (!hasRole) {
                        AssignedRole newRole = AssignedRole.builder()
                                        .roleName(role.getRoleName())
                                        .assignedAt(Instant.now(clock))
                                        .assignedBy(assignedBy != null && !assignedBy.isBlank() ? assignedBy : "SYSTEM")
                                        .build();

                        user.getRoles().add(newRole);
                        userRepository.save(user);
                        log.info("Assigned role '{}' to user '{}' by '{}'", roleName, userId, assignedBy);
                } else {
                        log.info("User '{}' already has role '{}'", userId, roleName);
                }
        }

        @Override
        public void replaceStaffRole(String userId, String roleName, String assignedBy) {
                Role role = roleRepository.findByRoleName(roleName)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                roleName + " role not found in database"));

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

                if (user.getRoles() == null) {
                        user.setRoles(new java.util.ArrayList<>());
                }

                user.getRoles().removeIf(r -> "ADMIN".equalsIgnoreCase(r.getRoleName())
                                || "EDITOR".equalsIgnoreCase(r.getRoleName()));

                user.getRoles().add(AssignedRole.builder()
                                .roleName(role.getRoleName())
                                .assignedAt(Instant.now(clock))
                                .assignedBy(assignedBy != null && !assignedBy.isBlank() ? assignedBy : "SYSTEM")
                                .build());

                userRepository.save(user);
                log.info("Replaced staff role with '{}' for user '{}' by '{}'", roleName, userId, assignedBy);
        }

        @Override
        public java.util.Set<String> getPermissionsByUserId(String userId) {
                List<String> roleNames = getRoleNamesByUserId(userId);
                if (roleNames.isEmpty()) {
                        return Collections.emptySet();
                }
                
                java.util.Set<String> allPermissions = new java.util.HashSet<>();
                for (String roleName : roleNames) {
                        roleRepository.findByRoleName(roleName).ifPresent(role -> {
                                if (role.getPermissions() != null) {
                                        allPermissions.addAll(role.getPermissions());
                                }
                        });
                }
                return allPermissions;
        }
}
