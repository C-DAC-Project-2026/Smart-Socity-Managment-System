package com.society.controller;

import com.society.dto.ApiResponse;
import com.society.dto.SocietyDTOs.*;
import com.society.service.SuperAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Platform-level society management. Every endpoint here requires
 * ROLE_SUPER_ADMIN — a Society Admin cannot reach any of these, even for
 * their own society (they manage their own society through the regular
 * resident/staff/notice/etc. endpoints, which are already scoped to them).
 */
@RestController
@RequestMapping("/api/super-admin/societies")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
@Tag(name = "Super Admin - Societies")
@SecurityRequirement(name = "bearerAuth")
public class SuperAdminController {

    private final SuperAdminService superAdminService;

    @PostMapping
    @Operation(summary = "Register a new society and its first Society Admin")
    public ResponseEntity<ApiResponse<SocietyResponse>> registerSociety(@Valid @RequestBody RegisterSocietyRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Society registered (status: PENDING)", superAdminService.registerSociety(request)));
    }

    @PutMapping("/{id}/activate")
    @Operation(summary = "Activate a society so its users can log in")
    public ResponseEntity<ApiResponse<SocietyResponse>> activate(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Society activated", superAdminService.activateSociety(id)));
    }

    @PutMapping("/{id}/suspend")
    @Operation(summary = "Suspend a society; all its users are immediately blocked from logging in")
    public ResponseEntity<ApiResponse<SocietyResponse>> suspend(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Society suspended", superAdminService.suspendSociety(id)));
    }

    @GetMapping
    @Operation(summary = "List all societies on the platform")
    public ResponseEntity<ApiResponse<List<SocietyResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Societies fetched", superAdminService.getAllSocieties()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single society")
    public ResponseEntity<ApiResponse<SocietyResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Society fetched", superAdminService.getSociety(id)));
    }
}
