package com.fonepay.devportal.startup;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fonepay.devportal.common.constant.enums.UserStatus;
import com.fonepay.devportal.common.util.IdGenerator;
import com.fonepay.devportal.modules.user.document.Role;
import com.fonepay.devportal.modules.user.document.User;
import com.fonepay.devportal.modules.user.document.UserRole;
import com.fonepay.devportal.modules.user.repository.RoleRepository;
import com.fonepay.devportal.modules.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(3)
@RequiredArgsConstructor
public class AdminUserSeeder implements CommandLineRunner {

    private static final String ADMIN_ROLE = "ADMIN";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    @Value("${app.seed.admin.email:}")
    private String adminEmail;

    @Value("${app.seed.admin.password:}")
    private String adminPassword;

    @Value("${app.seed.admin.full-name:System Admin}")
    private String adminFullName;

    @Override
    @Transactional
    public void run(String @NonNull... args) {
        if (isBlank(adminEmail) || isBlank(adminPassword)) {
            log.warn("Admin user seed skipped: set ADMIN_EMAIL and ADMIN_PASSWORD in .env");
            return;
        }

        Role adminRole = roleRepository.findByRoleName(ADMIN_ROLE)
                .orElseThrow(() -> new IllegalStateException(
                        "ADMIN role is missing. RolePermissionSeeder must run first."));

        String email = adminEmail.trim().toLowerCase();
        Instant now = Instant.now(clock);

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            user = new User();
            user.setUserId(IdGenerator.nextUlid());
            user.setEmail(email);
            user.setFullName(adminFullName == null || adminFullName.isBlank() ? "System Admin" : adminFullName.trim());
            user.setPasswordHash(passwordEncoder.encode(adminPassword));
            user.setStatus(UserStatus.ACTIVE);
            user.setEmailVerified(true);
            user.setRoles(new ArrayList<>());
            user.setCreatedAt(now);
            user.setUpdatedAt(now);
            user = userRepository.save(user);
            log.info("Seeded admin user {} ({})", email, user.getUserId());
        } else {
            user.setPasswordHash(passwordEncoder.encode(adminPassword));
            user.setStatus(UserStatus.ACTIVE);
            user.setEmailVerified(true);
            if (adminFullName != null && !adminFullName.isBlank()) {
                user.setFullName(adminFullName.trim());
            }
            user.setUpdatedAt(now);
            user = userRepository.save(user);
            log.info("Updated seed admin user {}", email);
        }

        ensureAdminRole(user, adminRole, now);
    }

    private void ensureAdminRole(User user, Role adminRole, Instant now) {
        if (user.getRoles() == null) {
            user.setRoles(new ArrayList<>());
        }
        boolean hasAdmin = user.getRoles().stream()
                .anyMatch(assigned -> ADMIN_ROLE.equalsIgnoreCase(assigned.getRoleName()));
        if (hasAdmin) {
            return;
        }

        user.getRoles().add(UserRole.builder()
                .id(IdGenerator.nextUlid())
                .user(user)
                .role(adminRole)
                .assignedAt(now)
                .assignedBy(user.getUserId())
                .build());
        userRepository.save(user);
        log.info("Assigned ADMIN role to {}", user.getEmail());
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
