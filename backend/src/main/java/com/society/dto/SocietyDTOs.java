package com.society.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

public class SocietyDTOs {

    /**
     * SUPER_ADMIN only. Creates a society AND its first Society Admin user
     * in a single atomic operation — there is no public self-service society
     * signup, which is what keeps tenant creation itself a controlled,
     * auditable action.
     */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class RegisterSocietyRequest {
        @NotBlank(message = "Society name is required")
        @Size(min = 2, max = 150)
        private String name;

        @NotBlank(message = "Society code is required")
        @Size(min = 2, max = 30)
        private String societyCode;

        @NotBlank(message = "Address is required")
        private String address;

        private String city;
        private String state;
        private String pincode;

        @NotBlank(message = "Contact email is required")
        @Email
        private String contactEmail;

        private String contactPhone;

        @NotBlank(message = "Admin name is required")
        private String adminName;

        @NotBlank(message = "Admin email is required")
        @Email
        private String adminEmail;

        @NotBlank(message = "Admin password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String adminPassword;

        // Only required (and only checked) on the PUBLIC self-service
        // registration endpoint — see PublicController. The SUPER_ADMIN's
        // own authenticated endpoint ignores these two fields entirely.
        private String captchaId;
        private String captchaAnswer;
    }

    /**
     * Minimal, public-safe view of a society — used to populate the
     * "select your society" dropdown that Residents/Staff see during
     * self-registration. Deliberately excludes contact details, status,
     * and timestamps.
     */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SocietyOption {
        private Long societyId;
        private String name;
        private String societyCode;
        private String city;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SocietyResponse {
        private Long societyId;
        private String name;
        private String societyCode;
        private String address;
        private String city;
        private String state;
        private String pincode;
        private String contactEmail;
        private String contactPhone;
        private String status;
        private LocalDateTime createdAt;
    }
}
