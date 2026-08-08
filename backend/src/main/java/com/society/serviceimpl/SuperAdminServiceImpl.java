package com.society.serviceimpl;

import com.society.dto.SocietyDTOs.*;
import com.society.entity.Role;
import com.society.entity.Society;
import com.society.entity.User;
import com.society.exception.BadRequestException;
import com.society.exception.ResourceNotFoundException;
import com.society.repository.RoleRepository;
import com.society.repository.SocietyRepository;
import com.society.repository.UserRepository;
import com.society.service.EmailService;
import com.society.service.SuperAdminService;
import com.society.util.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SuperAdminServiceImpl implements SuperAdminService {

    private final SocietyRepository societyRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.frontend.login-url}")
    private String loginUrl;

    @Override
    @Transactional
    public SocietyResponse registerSociety(RegisterSocietyRequest request) {
        if (societyRepository.existsBySocietyCode(request.getSocietyCode())) {
            throw new BadRequestException("Society code already in use: " + request.getSocietyCode());
        }
        if (userRepository.existsByEmail(request.getAdminEmail())) {
            throw new BadRequestException("Email already registered: " + request.getAdminEmail());
        }

        // New societies start PENDING: the tenant exists, but no one can log
        // in yet (CustomUserDetails.isEnabled() requires ACTIVE) until
        // SUPER_ADMIN explicitly activates it. This makes onboarding a
        // deliberate two-step action instead of instant self-service.
        Society society = Society.builder()
            .name(request.getName())
            .societyCode(request.getSocietyCode())
            .address(request.getAddress())
            .city(request.getCity())
            .state(request.getState())
            .pincode(request.getPincode())
            .contactEmail(request.getContactEmail())
            .contactPhone(request.getContactPhone())
            .status(Society.Status.PENDING)
            .build();
        society = societyRepository.save(society);

        Role adminRole = roleRepository.findByRoleName(AppConstants.ROLE_ADMIN)
            .orElseThrow(() -> new ResourceNotFoundException("Role not seeded: " + AppConstants.ROLE_ADMIN));

        User admin = User.builder()
            .name(request.getAdminName())
            .email(request.getAdminEmail())
            .password(passwordEncoder.encode(request.getAdminPassword()))
            .role(adminRole)
            .society(society)
            .active(true)
            .build();
        userRepository.save(admin);

        // Notify every platform SUPER_ADMIN that a new society is waiting for review.
        List<String> superAdminEmails = userRepository.findByRole_RoleName(AppConstants.ROLE_SUPER_ADMIN)
            .stream().map(User::getEmail).collect(Collectors.toList());
        emailService.sendToAll(superAdminEmails,
            "New society registration pending approval: " + society.getName(),
            "A new society has registered on Smart Society and is awaiting your approval.\n\n" +
            "Society: " + society.getName() + " (" + society.getSocietyCode() + ")\n" +
            "City: " + (society.getCity() != null ? society.getCity() : "-") + "\n" +
            "Admin name: " + admin.getName() + "\n" +
            "Admin email: " + admin.getEmail() + "\n\n" +
            "Log in to the platform dashboard to review and activate it:\n" + loginUrl + "\n\n" +
            "- Smart Society Management System"
        );

        return toDTO(society);
    }

    @Override
    @Transactional
    public SocietyResponse activateSociety(Long societyId) {
        Society society = findById(societyId);
        society.setStatus(Society.Status.ACTIVE);
        Society saved = societyRepository.save(society);

        // Notify the society's Admin that they can now log in.
        List<User> admins = userRepository.findByRole_RoleNameAndSociety_SocietyId(AppConstants.ROLE_ADMIN, societyId);
        for (User admin : admins) {
            emailService.send(admin.getEmail(),
                "Your society \"" + saved.getName() + "\" has been approved",
                "Hi " + admin.getName() + ",\n\n" +
                "Good news - your society \"" + saved.getName() + "\" (" + saved.getSocietyCode() + ") " +
                "has been reviewed and approved by the platform admin. You and your residents/staff can now log in.\n\n" +
                "Log in here: " + loginUrl + "\n\n" +
                "- Smart Society Management System"
            );
        }

        return toDTO(saved);
    }

    @Override
    @Transactional
    public SocietyResponse suspendSociety(Long societyId) {
        Society society = findById(societyId);
        society.setStatus(Society.Status.SUSPENDED);
        return toDTO(societyRepository.save(society));
    }

    @Override
    public SocietyResponse getSociety(Long societyId) {
        return toDTO(findById(societyId));
    }

    @Override
    public List<SocietyResponse> getAllSocieties() {
        return societyRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<SocietyOption> getActiveSocietyOptions() {
        return societyRepository.findByStatus(Society.Status.ACTIVE).stream()
            .map(s -> SocietyOption.builder()
                .societyId(s.getSocietyId())
                .name(s.getName())
                .societyCode(s.getSocietyCode())
                .city(s.getCity())
                .build())
            .collect(Collectors.toList());
    }

    private Society findById(Long id) {
        return societyRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Society not found: " + id));
    }

    private SocietyResponse toDTO(Society s) {
        return SocietyResponse.builder()
            .societyId(s.getSocietyId())
            .name(s.getName())
            .societyCode(s.getSocietyCode())
            .address(s.getAddress())
            .city(s.getCity())
            .state(s.getState())
            .pincode(s.getPincode())
            .contactEmail(s.getContactEmail())
            .contactPhone(s.getContactPhone())
            .status(s.getStatus().name())
            .createdAt(s.getCreatedAt())
            .build();
    }
}
