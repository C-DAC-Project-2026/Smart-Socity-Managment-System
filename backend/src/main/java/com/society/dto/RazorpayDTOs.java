package com.society.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

public class RazorpayDTOs {

    // Step 1: Frontend asks backend to create an order
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class CreateOrderRequest {
        @NotNull(message = "Bill ID is required")
        private Long billId;
    }

    // Step 2: Backend returns order details to frontend
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateOrderResponse {
        private String  orderId;        // Razorpay order ID (order_XXXX)
        private Long    amount;         // amount in paise (₹2500 = 250000)
        private String  currency;       // INR
        private String  keyId;          // Razorpay key ID for frontend
        private String  residentName;
        private String  description;    // e.g. "Maintenance Bill - March 2025"
    }

    // Step 3: Frontend sends payment confirmation after user pays
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class PaymentVerifyRequest {
        @NotNull(message = "Bill ID is required")
        private Long billId;

        @NotBlank(message = "Razorpay order ID is required")
        private String razorpayOrderId;

        @NotBlank(message = "Razorpay payment ID is required")
        private String razorpayPaymentId;

        @NotBlank(message = "Razorpay signature is required")
        private String razorpaySignature;

        private String paymentMode;     // UPI, CARD, NETBANKING etc
    }
}
