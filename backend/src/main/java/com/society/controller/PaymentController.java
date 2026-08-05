package com.society.controller;

import com.society.dto.ApiResponse;
import com.society.dto.PaymentDTO;
import com.society.service.PaymentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/payments")
@RequiredArgsConstructor @Tag(name = "Payments") @SecurityRequirement(name = "bearerAuth")
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','RESIDENT')")
    public ResponseEntity<ApiResponse<PaymentDTO>> pay(@Valid @RequestBody PaymentDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Payment successful", paymentService.makePayment(dto)));
    }
    @GetMapping @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<PaymentDTO>>> getAll(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Payments fetched",
            paymentService.getAllPayments(PageRequest.of(page, size, Sort.by("paymentDate").descending()))));
    }
    @GetMapping("/resident/{residentId}") @PreAuthorize("hasAnyRole('ADMIN','RESIDENT')")
    public ResponseEntity<ApiResponse<Page<PaymentDTO>>> getByResident(@PathVariable Long residentId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Payments fetched",
            paymentService.getPaymentsByResident(residentId, PageRequest.of(page, size, Sort.by("paymentDate").descending()))));
    }
    @GetMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','RESIDENT')")
    public ResponseEntity<ApiResponse<PaymentDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Payment fetched", paymentService.getPaymentById(id)));
    }
}
