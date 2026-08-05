package com.society.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ComplaintDTO {

    private Long   complaintId;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must be under 200 characters")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    private String        status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Resident info
    private Long   residentId;
    private String residentName;
    private String flatNo;

    // Staff info (nullable)
    private Long   assignedStaffId;
    private String assignedStaffName;
    private String department;
}
