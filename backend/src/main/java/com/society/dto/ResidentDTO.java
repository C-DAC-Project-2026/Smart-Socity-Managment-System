package com.society.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ResidentDTO {

    private Long   residentId;
    private Long   userId;
    private String name;
    private String email;

    /** False while awaiting Society Admin approval after self-registration. */
    private Boolean active;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Mobile is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Enter a valid 10-digit mobile number")
    private String mobile;

    @NotBlank(message = "Flat number is required")
    private String flatNo;
}
