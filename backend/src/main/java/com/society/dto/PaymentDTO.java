package com.society.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentDTO {

    private Long paymentId;

    @NotNull(message = "Bill ID is required")
    private Long billId;

    private BigDecimal    amount;
    private LocalDateTime paymentDate;

    @NotBlank(message = "Payment mode is required")
    private String paymentMode;   // ONLINE | CASH | CHEQUE | UPI | NEFT

    private String transactionId;
    private String status;

    // Display fields
    private String residentName;
    private String flatNo;
    private Integer billMonth;
    private Integer billYear;
}
