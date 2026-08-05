package com.society.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ComplaintStatusUpdateDTO {

    @NotBlank(message = "Status is required")
    private String status;   // PENDING | ASSIGNED | IN_PROGRESS | RESOLVED

    @Size(max = 500, message = "Remarks must be under 500 characters")
    private String remarks;

    private Long staffId;    // used when assigning complaint to staff
}
