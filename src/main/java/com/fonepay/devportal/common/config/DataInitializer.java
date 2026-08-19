package com.fonepay.devportal.common.config;

import com.fonepay.devportal.common.util.IdGenerator;
import com.fonepay.devportal.modules.user.entity.Role;
import com.fonepay.devportal.modules.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final Clock clock;

    @Override
    public void run(String... args) throws Exception {
        log.info("Checking and initializing default roles...");
        
        List<String> defaultRoles = List.of("DEVELOPER", "ADMIN", "EDITOR");
        
        for (String roleName : defaultRoles) {
            if (roleRepository.findByRoleName(roleName).isEmpty()) {
                Role role = Role.builder()
                        .roleId(IdGenerator.nextUlid())
                        .roleName(roleName)
                        .description("Default " + roleName + " role")
                        .createdAt(Instant.now(clock))
                        .build();
                roleRepository.save(role);
                log.info("Created default role: {}", roleName);
            }
        }
    }
}
