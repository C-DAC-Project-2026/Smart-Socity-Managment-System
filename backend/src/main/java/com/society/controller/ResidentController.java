package com.society.controller;

import com.society.dto.ApiResponse;
import com.society.dto.ResidentDTO;
import com.society.service.ResidentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/residents")
@RequiredArgsConstructor
@Tag(name = "Residents")
@SecurityRequirement(name = "bearerAuth")
public class ResidentController {

    private final ResidentService residentService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all residents")
    public ResponseEntity<ApiResponse<List<ResidentDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Residents fetched", residentService.getAllResidents()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RESIDENT')")
    public ResponseEntity<ApiResponse<ResidentDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Resident fetched", residentService.getResidentById(id)));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','RESIDENT')")
    public ResponseEntity<ApiResponse<ResidentDTO>> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Resident fetched", residentService.getResidentByUserId(userId)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RESIDENT')")
    public ResponseEntity<ApiResponse<ResidentDTO>> update(@PathVariable Long id, @Valid @RequestBody ResidentDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Resident updated", residentService.updateResident(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        residentService.deleteResident(id);
        return ResponseEntity.ok(ApiResponse.success("Resident deleted"));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ResidentDTO>>> search(@RequestParam String q) {
        return ResponseEntity.ok(ApiResponse.success("Search results", residentService.searchResidents(q)));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List self-registered residents awaiting approval")
    public ResponseEntity<ApiResponse<List<ResidentDTO>>> getPending() {
        return ResponseEntity.ok(ApiResponse.success("Pending residents fetched", residentService.getPendingResidents()));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve a self-registered resident so they can log in")
    public ResponseEntity<ApiResponse<ResidentDTO>> approve(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Resident approved", residentService.approveResident(id)));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reject and remove a self-registered resident")
    public ResponseEntity<ApiResponse<Void>> reject(@PathVariable Long id) {
        residentService.rejectResident(id);
        return ResponseEntity.ok(ApiResponse.success("Resident registration rejected"));
    }
}
