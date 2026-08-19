package com.fonepay.devportal.modules.user.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fonepay.devportal.common.exception.ResourceNotFoundException;
import com.fonepay.devportal.common.util.IdGenerator;
import com.fonepay.devportal.modules.user.document.Role;
import com.fonepay.devportal.modules.user.document.UserRole;
import com.fonepay.devportal.modules.user.repository.RoleRepository;
import com.fonepay.devportal.modules.user.repository.UserRoleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRoleServiceImpl implements UserRoleService {

    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final Clock clock;

    @Override
    public List<String> getRoleNamesByUserId(String userId) {
        return userRoleRepository.findByUserId(userId).stream()
                .map(ur -> roleRepository.findById(ur.getRoleId()).map(Role::getRoleName).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public void assignDefaultRole(String userId, String roleName) {
        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException(roleName + " role not found in database"));

        UserRole userRole = UserRole.builder()
                .id(IdGenerator.nextUlid())
                .userId(userId)
                .roleId(role.getRoleId())
                .assignedAt(Instant.now(clock))
                .build();

        userRoleRepository.save(userRole);
        log.info("Assigned role '{}' to user '{}'", roleName, userId);
    }
}
