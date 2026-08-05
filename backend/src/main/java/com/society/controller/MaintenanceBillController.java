package com.society.controller;

import com.society.dto.ApiResponse;
import com.society.dto.MaintenanceBillDTO;
import com.society.service.MaintenanceBillService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/bills")
@RequiredArgsConstructor @Tag(name = "Maintenance Bills") @SecurityRequirement(name = "bearerAuth")
public class MaintenanceBillController {
    private final MaintenanceBillService billService;

    @PostMapping @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MaintenanceBillDTO>> create(@Valid @RequestBody MaintenanceBillDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Bill created", billService.createBill(dto)));
    }
    @PostMapping("/generate-all") @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> generateAll(@RequestParam int month, @RequestParam int year, @RequestParam double amount) {
        billService.generateBillsForAll(month, year, amount);
        return ResponseEntity.ok(ApiResponse.success("Bills generated for all residents for " + month + "/" + year));
    }
    @GetMapping @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<MaintenanceBillDTO>>> getAll(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Bills fetched",
            billService.getAllBills(PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }
    @GetMapping("/resident/{residentId}") @PreAuthorize("hasAnyRole('ADMIN','RESIDENT')")
    public ResponseEntity<ApiResponse<Page<MaintenanceBillDTO>>> getByResident(@PathVariable Long residentId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Bills fetched",
            billService.getBillsByResident(residentId, PageRequest.of(page, size, Sort.by("year").descending().and(Sort.by("month").descending())))));
    }
    @GetMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','RESIDENT')")
    public ResponseEntity<ApiResponse<MaintenanceBillDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Bill fetched", billService.getBillById(id)));
    }
}
