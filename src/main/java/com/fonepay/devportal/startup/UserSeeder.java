package com.fonepay.devportal.startup;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;

import org.jspecify.annotations.NonNull;
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
public class UserSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    @Override
    @Transactional
    public void run(String @NonNull... args) {
        String defaultPassword = "Password123!";
        Instant now = clock.instant();

        seedUser("admin@example.com", "Admin User", "ADMIN", defaultPassword, now);
        seedUser("editor@example.com", "Editor User", "EDITOR", defaultPassword, now);
        seedUser("developer@example.com", "Developer User", "DEVELOPER", defaultPassword, now);
        
        // Seed your specific email as an ADMIN
        seedUser("nishantkoirala16@gmail.com", "Nishant Koirala", "ADMIN", defaultPassword, now);
    }

    private void seedUser(String email, String fullName, String roleName, String rawPassword, Instant now) {
        if (userRepository.existsByEmail(email)) {
            log.info("Test user {} already exists. Skipping.", email);
            return;
        }

        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new IllegalStateException("Role not found: " + roleName));

        User user = new User();
        user.setUserId(IdGenerator.nextUlid());
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(true);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setRoles(new ArrayList<>());

        UserRole userRole = new UserRole();
        userRole.setId(IdGenerator.nextUlid());
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setAssignedAt(now);
        userRole.setAssignedBy("SYSTEM");
        
        user.getRoles().add(userRole);

        userRepository.save(user);
        log.info("Created test user: {} with role: {}", email, roleName);
    }
}
