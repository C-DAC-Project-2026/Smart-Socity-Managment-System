package com.society.config;

import com.society.entity.Role;
import com.society.entity.User;
import com.society.repository.RoleRepository;
import com.society.repository.UserRepository;
import com.society.util.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Idempotent startup seeder — safe to run on every boot.
 *
 * 1. Ensures all four roles exist (previously these had to be inserted by
 *    hand; that doesn't scale to a deployable multi-tenant app).
 * 2. Ensures exactly one bootstrap SUPER_ADMIN account exists, so there is
 *    always a way to log in and start registering societies on a fresh
 *    database. Credentials come from app.super-admin.* — change the
 *    password immediately after first login in a real deployment.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.super-admin.email}")
    private String superAdminEmail;

    @Value("${app.super-admin.password}")
    private String superAdminPassword;

    @Override
    @Transactional
    public void run(String... args) {
        seedRole(AppConstants.ROLE_SUPER_ADMIN);
        seedRole(AppConstants.ROLE_ADMIN);
        seedRole(AppConstants.ROLE_RESIDENT);
        seedRole(AppConstants.ROLE_STAFF);

        if (!userRepository.existsByEmail(superAdminEmail)) {
            Role superAdminRole = roleRepository.findByRoleName(AppConstants.ROLE_SUPER_ADMIN).orElseThrow();
            User superAdmin = User.builder()
                .name("Platform Super Admin")
                .email(superAdminEmail)
                .password(passwordEncoder.encode(superAdminPassword))
                .role(superAdminRole)
                .society(null) // SUPER_ADMIN is not scoped to any society
                .active(true)
                .build();
            userRepository.save(superAdmin);
            log.warn("Bootstrap SUPER_ADMIN created ({}). Change this password immediately.", superAdminEmail);
        }
    }

    private void seedRole(String roleName) {
        if (roleRepository.findByRoleName(roleName).isEmpty()) {
            Role role = new Role();
            role.setRoleName(roleName);
            roleRepository.save(role);
            log.info("Seeded role: {}", roleName);
        }
    }
}
