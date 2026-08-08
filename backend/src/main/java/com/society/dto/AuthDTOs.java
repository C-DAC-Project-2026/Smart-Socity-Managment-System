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

    /**
     * PUBLIC self-registration for a RESIDENT or STAFF user. Unlike
     * {@link RegisterRequest} (which is only reachable by an authenticated
     * Society Admin), this endpoint is open to anyone on the internet, so
     * it carries its own captcha AND an explicit societyId chosen by the
     * user from the public society list. The new account is created
     * DEACTIVATED (active = false) and only becomes usable once the
     * Society Admin of that society approves it.
     */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class PublicRegisterRequest {
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

        @NotNull(message = "Please select your society")
        private Long societyId;

        // Resident-specific fields (required when role = ROLE_RESIDENT)
        private String address;
        private String mobile;
        private String flatNo;

        // Staff-specific fields (required when role = ROLE_STAFF)
        private String department;

        @NotBlank(message = "Captcha id is required")
        private String captchaId;

        @NotBlank(message = "Captcha answer is required")
        private String captchaAnswer;
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

        // No captcha here: this endpoint requires an authenticated Society
        // Admin (see AuthController), so bot/anti-automation protection is
        // unnecessary — captcha is only for the public, unauthenticated
        // login/forgot-password endpoints below.
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
