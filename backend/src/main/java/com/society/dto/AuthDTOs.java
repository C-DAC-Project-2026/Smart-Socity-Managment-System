package com.society.dto;

import jakarta.validation.constraints.*;
import lombok.*;

public class AuthDTOs {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;

        @NotBlank(message = "Captcha id is required")
        private String captchaId;

        @NotBlank(message = "Captcha answer is required")
        private String captchaAnswer;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class LoginResponse {
        private String token;
        private String tokenType = "Bearer";
        private Long   userId;
        private String name;
        private String email;
        private String role;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class RegisterRequest {
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100, message = "Name must be 2-100 characters")
        private String name;

        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;

        @NotBlank(message = "Role is required")
        private String role;   // ROLE_RESIDENT | ROLE_STAFF

        // Resident-specific fields (optional depending on role)
        private String address;
        private String mobile;
        private String flatNo;

        // Staff-specific fields (optional depending on role)
        private String department;

        @NotBlank(message = "Captcha id is required")
        private String captchaId;

        @NotBlank(message = "Captcha answer is required")
        private String captchaAnswer;
    }

    // ---- Captcha ----
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class CaptchaResponse {
        private String captchaId;
        private String question;
    }

    // ---- Forgot / Reset Password ----
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class ForgotPasswordRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email")
        private String email;

        @NotBlank(message = "Captcha id is required")
        private String captchaId;

        @NotBlank(message = "Captcha answer is required")
        private String captchaAnswer;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class ResetPasswordRequest {
        @NotBlank(message = "Reset token is required")
        private String token;

        @NotBlank(message = "New password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String newPassword;
    }
}
