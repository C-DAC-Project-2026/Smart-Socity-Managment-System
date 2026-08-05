package com.society.serviceimpl;

import com.society.dto.AuthDTOs.*;
import com.society.entity.*;
import com.society.exception.BadRequestException;
import com.society.exception.ResourceNotFoundException;
import com.society.repository.*;
import com.society.security.JwtTokenProvider;
import com.society.service.AuthService;
import com.society.service.CaptchaService;
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
        if (!captchaService.verify(request.getCaptchaId(), request.getCaptchaAnswer())) {
            throw new BadRequestException("Incorrect captcha answer. Please try again.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered: " + request.getEmail());
        }

        Role role = roleRepository.findByRoleName(request.getRole())
            .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + request.getRole()));

        User user = User.builder()
            .name(request.getName())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(role)
            .build();
        user = userRepository.save(user);

        if ("ROLE_RESIDENT".equals(request.getRole())) {
            if (request.getFlatNo() != null && residentRepository.existsByFlatNo(request.getFlatNo())) {
                throw new BadRequestException("Flat number already registered: " + request.getFlatNo());
            }
            Resident resident = Resident.builder()
                .address(request.getAddress())
                .mobile(request.getMobile())
                .flatNo(request.getFlatNo())
                .user(user)
                .build();
            residentRepository.save(resident);
        } else if ("ROLE_STAFF".equals(request.getRole())) {
            Staff staff = Staff.builder()
                .department(request.getDepartment())
                .mobile(request.getMobile())
                .user(user)
                .build();
            staffRepository.save(staff);
        }

        return "User registered successfully with role: " + request.getRole();
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
