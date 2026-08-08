package com.society.serviceimpl;

import com.society.dto.AuthDTOs.*;
import com.society.entity.*;
import com.society.exception.BadRequestException;
import com.society.exception.ResourceNotFoundException;
import com.society.repository.*;
import com.society.security.JwtTokenProvider;
import com.society.security.SecurityUtils;
import com.society.service.AuthService;
import com.society.service.CaptchaService;
import com.society.util.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider      jwtTokenProvider;
    private final UserRepository        userRepository;
    private final RoleRepository        roleRepository;
    private final ResidentRepository    residentRepository;
    private final StaffRepository       staffRepository;
    private final PasswordEncoder       passwordEncoder;
    private final CaptchaService        captchaService;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final JavaMailSender        mailSender;
    private final SocietyRepository     societyRepository;
    private final SecurityUtils         securityUtils;

    @Value("${app.frontend.reset-password-url}")
    private String resetPasswordUrl;

    @Value("${app.reset-token.expiry-minutes}")
    private long resetTokenExpiryMinutes;

    @Value("${spring.mail.username}")
    private String mailFrom;

    @Override
    public LoginResponse login(LoginRequest request) {
        if (!captchaService.verify(request.getCaptchaId(), request.getCaptchaAnswer())) {
            throw new BadRequestException("Incorrect captcha answer. Please try again.");
        }

        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        String token = jwtTokenProvider.generateToken(authentication);
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return LoginResponse.builder()
            .token(token)
            .tokenType("Bearer")
            .userId(user.getUserId())
            .name(user.getName())
            .email(user.getEmail())
            .role(user.getRole().getRoleName())
            .build();
    }

    @Override
    @Transactional
    public String register(RegisterRequest request) {
        // This endpoint is only for a Society Admin populating their own society
        // with RESIDENT / STAFF accounts. ADMIN and SUPER_ADMIN accounts are
        // never created here — Society Admins are created by SUPER_ADMIN when
        // the society itself is registered (see SuperAdminServiceImpl).
        if (!AppConstants.ROLE_RESIDENT.equals(request.getRole()) && !AppConstants.ROLE_STAFF.equals(request.getRole())) {
            throw new BadRequestException("This endpoint can only register RESIDENT or STAFF users");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered: " + request.getEmail());
        }

        Role role = roleRepository.findByRoleName(request.getRole())
            .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + request.getRole()));

        // societyId comes ONLY from the authenticated admin's own JWT-derived
        // context — never from the request body — so a tampered request can
        // never plant a user into a different society.
        Society society = societyRepository.findById(securityUtils.getCurrentSocietyId())
            .orElseThrow(() -> new ResourceNotFoundException("Society not found"));

        User user = User.builder()
            .name(request.getName())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(role)
            .society(society)
            .build();
        user = userRepository.save(user);

        if (AppConstants.ROLE_RESIDENT.equals(request.getRole())) {
            if (request.getFlatNo() != null
                    && residentRepository.existsByFlatNoAndSociety_SocietyId(request.getFlatNo(), society.getSocietyId())) {
                throw new BadRequestException("Flat number already registered: " + request.getFlatNo());
            }
            Resident resident = Resident.builder()
                .address(request.getAddress())
                .mobile(request.getMobile())
                .flatNo(request.getFlatNo())
                .user(user)
                .society(society)
                .build();
            residentRepository.save(resident);
        } else if (AppConstants.ROLE_STAFF.equals(request.getRole())) {
            Staff staff = Staff.builder()
                .department(request.getDepartment())
                .mobile(request.getMobile())
                .user(user)
                .society(society)
                .build();
            staffRepository.save(staff);
        }

        return "User registered successfully with role: " + request.getRole();
    }

    @Override
    @Transactional
    public String registerPublic(PublicRegisterRequest request) {
        if (!captchaService.verify(request.getCaptchaId(), request.getCaptchaAnswer())) {
            throw new BadRequestException("Incorrect captcha answer. Please try again.");
        }

        if (!AppConstants.ROLE_RESIDENT.equals(request.getRole()) && !AppConstants.ROLE_STAFF.equals(request.getRole())) {
            throw new BadRequestException("Role must be either ROLE_RESIDENT or ROLE_STAFF");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered: " + request.getEmail());
        }

        Society society = societyRepository.findById(request.getSocietyId())
            .orElseThrow(() -> new ResourceNotFoundException("Selected society was not found"));

        // Residents/Staff may only self-register into a society that has
        // already been approved and activated by SUPER_ADMIN. A PENDING or
        // SUSPENDED society cannot accept new signups.
        if (society.getStatus() != Society.Status.ACTIVE) {
            throw new BadRequestException("This society is not accepting registrations right now. Please contact your society admin.");
        }

        Role role = roleRepository.findByRoleName(request.getRole())
            .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + request.getRole()));

        if (AppConstants.ROLE_RESIDENT.equals(request.getRole())) {
            if (request.getFlatNo() == null || request.getFlatNo().isBlank()) {
                throw new BadRequestException("Flat number is required");
            }
            if (residentRepository.existsByFlatNoAndSociety_SocietyId(request.getFlatNo(), society.getSocietyId())) {
                throw new BadRequestException("Flat number already registered: " + request.getFlatNo());
            }
        } else if (request.getDepartment() == null || request.getDepartment().isBlank()) {
            throw new BadRequestException("Department is required");
        }

        // New self-registered accounts start INACTIVE — they cannot log in
        // (see CustomUserDetails.isAccountNonLocked()) until the Society
        // Admin of this society reviews and approves them.
        User user = User.builder()
            .name(request.getName())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(role)
            .society(society)
            .active(false)
            .build();
        user = userRepository.save(user);

        if (AppConstants.ROLE_RESIDENT.equals(request.getRole())) {
            Resident resident = Resident.builder()
                .address(request.getAddress())
                .mobile(request.getMobile())
                .flatNo(request.getFlatNo())
                .user(user)
                .society(society)
                .build();
            residentRepository.save(resident);
        } else {
            Staff staff = Staff.builder()
                .department(request.getDepartment())
                .mobile(request.getMobile())
                .user(user)
                .society(society)
                .build();
            staffRepository.save(staff);
        }

        return "Registration submitted for " + society.getName() + ". Your account is pending approval by your society admin.";
    }

    @Override
    @Transactional
    public String forgotPassword(ForgotPasswordRequest request) {
        if (!captchaService.verify(request.getCaptchaId(), request.getCaptchaAnswer())) {
            throw new BadRequestException("Incorrect captcha answer. Please try again.");
        }

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("No account found with email: " + request.getEmail()));

        // Remove any previous reset token for this user so old links stop working
        resetTokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
            .token(token)
            .user(user)
            .expiryDate(LocalDateTime.now().plusMinutes(resetTokenExpiryMinutes))
            .build();
        resetTokenRepository.save(resetToken);

        String link = resetPasswordUrl + "?token=" + token;

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(user.getEmail());
            message.setSubject("Smart Society - Reset your password");
            message.setText(
                "Hi " + user.getName() + ",\n\n" +
                "We received a request to reset your password.\n" +
                "Click the link below to set a new password (valid for " + resetTokenExpiryMinutes + " minutes):\n\n" +
                link + "\n\n" +
                "If you didn't request this, you can safely ignore this email.\n\n" +
                "- Smart Society Management System"
            );
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", user.getEmail(), e.getMessage());
            throw new BadRequestException("Could not send reset email. Please check the mail server configuration and try again.");
        }

        return "Password reset link sent to " + user.getEmail();
    }

    @Override
    @Transactional
    public String resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = resetTokenRepository.findByToken(request.getToken())
            .orElseThrow(() -> new BadRequestException("Invalid or already-used reset link."));

        if (resetToken.isExpired()) {
            resetTokenRepository.delete(resetToken);
            throw new BadRequestException("This reset link has expired. Please request a new one.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetTokenRepository.delete(resetToken); // one-time use

        return "Password reset successful. You can now log in with your new password.";
    }
}
