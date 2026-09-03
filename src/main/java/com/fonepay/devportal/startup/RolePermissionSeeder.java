package com.fonepay.devportal.startup;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fonepay.devportal.common.util.IdGenerator;
import com.fonepay.devportal.modules.user.document.Role;
import com.fonepay.devportal.modules.user.repository.RoleRepository;
import com.fonepay.devportal.security.Permissions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class RolePermissionSeeder implements CommandLineRunner {

    private static final Set<String> ADMIN_PERMISSIONS = Set.of(
            Permissions.CMS_PAGE_CREATE,
            Permissions.CMS_PAGE_EDIT,
            Permissions.CMS_PAGE_SUBMIT,
            Permissions.CMS_PAGE_APPROVE,
            Permissions.CMS_PAGE_PUBLISH,
            Permissions.USER_INVITE,
            Permissions.USER_MANAGE,
            Permissions.PRODUCT_MANAGE,
            Permissions.DOCUMENT_VIEW,
            Permissions.SYSTEM_MANAGE);

    private static final Set<String> EDITOR_PERMISSIONS = Set.of(
            Permissions.CMS_PAGE_CREATE,
            Permissions.CMS_PAGE_EDIT,
            Permissions.CMS_PAGE_SUBMIT,
            Permissions.DOCUMENT_VIEW);

    private static final Set<String> DEVELOPER_PERMISSIONS = Set.of(
            Permissions.DOCUMENT_VIEW);

    private final RoleRepository roleRepository;
    private final Clock clock;

    @Override
    @Transactional
    public void run(String @NonNull... args) {
        Instant now = Instant.now(clock);
        upsertRole("ADMIN", "Full administrative access", ADMIN_PERMISSIONS, now);
        upsertRole("EDITOR", "CMS authoring access", EDITOR_PERMISSIONS, now);
        upsertRole("DEVELOPER", "Developer portal access", DEVELOPER_PERMISSIONS, now);
    }

    private void upsertRole(String roleName, String description, Set<String> permissions, Instant now) {
        Role role = roleRepository.findByRoleName(roleName).orElseGet(() -> Role.builder()
                .roleId(IdGenerator.nextUlid())
                .roleName(roleName)
                .createdAt(now)
                .build());
        role.setDescription(description);
        role.setPermissions(new LinkedHashSet<>(permissions));
        roleRepository.save(role);
        log.info("Ensured role [{}] with {} permissions", roleName, permissions.size());
    }
}
