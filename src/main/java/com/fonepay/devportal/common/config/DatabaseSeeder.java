package com.fonepay.devportal.common.config;

import java.time.Clock;
import java.time.Instant;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.fonepay.devportal.modules.user.document.Role;
import com.fonepay.devportal.modules.user.repository.RoleRepository;
import com.fonepay.devportal.security.Permissions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final Clock clock;

    @Override
    public void run(String... args) {
        log.info("Starting database seeding process...");
        seedRolesAndPermissions();
        log.info("Database seeding process completed.");
    }

    private void seedRolesAndPermissions() {
        seedRole("ADMIN", "System Administrator", Set.of(
                Permissions.CMS_PAGE_CREATE,
                Permissions.CMS_PAGE_EDIT,
                Permissions.CMS_PAGE_SUBMIT,
                Permissions.CMS_PAGE_APPROVE,
                Permissions.CMS_PAGE_PUBLISH,
                Permissions.USER_INVITE,
                Permissions.USER_MANAGE,
                Permissions.PRODUCT_MANAGE,
                Permissions.DOCUMENT_VIEW
        ));

        seedRole("EDITOR", "Content Editor", Set.of(
                Permissions.CMS_PAGE_CREATE,
                Permissions.CMS_PAGE_EDIT,
                Permissions.CMS_PAGE_SUBMIT,
                Permissions.DOCUMENT_VIEW
        ));

        seedRole("DEVELOPER", "Developer/API Consumer", Set.of(
                Permissions.DOCUMENT_VIEW
        ));
    }

    private void seedRole(String roleName, String description, Set<String> permissions) {
        roleRepository.findByRoleName(roleName).ifPresentOrElse(
                existingRole -> {
                    boolean updated = false;
                    if (existingRole.getPermissions() == null || !existingRole.getPermissions().containsAll(permissions)) {
                        existingRole.setPermissions(permissions);
                        updated = true;
                    }
                    if (existingRole.getDescription() == null || !existingRole.getDescription().equals(description)) {
                        existingRole.setDescription(description);
                        updated = true;
                    }
                    if (updated) {
                        roleRepository.save(existingRole);
                        log.info("Updated existing role '{}' with necessary permissions.", roleName);
                    }
                },
                () -> {
                    Role newRole = Role.builder()
                            .roleName(roleName)
                            .description(description)
                            .permissions(permissions)
                            .createdAt(Instant.now(clock))
                            .build();
                    roleRepository.save(newRole);
                    log.info("Created new role '{}' with permissions.", roleName);
                }
        );
    }
}
