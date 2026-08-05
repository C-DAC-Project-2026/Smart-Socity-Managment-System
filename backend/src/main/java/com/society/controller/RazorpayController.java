package com.society.controller;

import com.society.dto.ApiResponse;
import com.society.dto.RazorpayDTOs.*;
import com.society.service.RazorpayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/razorpay")
@RequiredArgsConstructor
@Tag(name = "Razorpay Payment Gateway")
@SecurityRequirement(name = "bearerAuth")
public class RazorpayController {

    private final RazorpayService razorpayService;

    // Called when user clicks "Pay Now"
    // Returns: orderId, amount, keyId for frontend Razorpay popup
    @PostMapping("/create-order")
    @PreAuthorize("hasAnyRole('ADMIN','RESIDENT')")
    @Operation(summary = "Create Razorpay order for a bill")
    public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(
            ApiResponse.success("Order created", razorpayService.createOrder(request.getBillId()))
        );
    }

    // Called after user completes payment in Razorpay popup
    // Verifies signature and saves payment to DB
    @PostMapping("/verify-payment")
    @PreAuthorize("hasAnyRole('ADMIN','RESIDENT')")
    @Operation(summary = "Verify Razorpay payment and mark bill as paid")
    public ResponseEntity<ApiResponse<String>> verifyPayment(
            @Valid @RequestBody PaymentVerifyRequest request) {
        razorpayService.verifyAndSavePayment(request);
        return ResponseEntity.ok(ApiResponse.success("Payment verified and saved successfully"));
    }
}
