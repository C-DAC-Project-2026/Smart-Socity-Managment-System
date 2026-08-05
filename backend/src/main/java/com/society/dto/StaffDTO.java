package com.society.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StaffDTO {

    private Long   staffId;
    private Long   userId;
    private String name;
    private String email;

    @NotBlank(message = "Department is required")
    private String department;

    @NotBlank(message = "Mobile is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Enter a valid 10-digit mobile number")
    private String mobile;
}
