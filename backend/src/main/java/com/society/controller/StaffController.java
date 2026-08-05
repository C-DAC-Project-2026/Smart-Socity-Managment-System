package com.society.controller;

import com.society.dto.ApiResponse;
import com.society.dto.StaffDTO;
import com.society.service.StaffService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/staff")
@RequiredArgsConstructor @Tag(name = "Staff") @SecurityRequirement(name = "bearerAuth")
public class StaffController {
    private final StaffService staffService;

    @GetMapping @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<StaffDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Staff fetched", staffService.getAllStaff()));
    }
    @GetMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<StaffDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Staff fetched", staffService.getStaffById(id)));
    }
    @GetMapping("/user/{userId}") @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<StaffDTO>> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Staff fetched", staffService.getStaffByUserId(userId)));
    }
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<StaffDTO>> update(@PathVariable Long id, @Valid @RequestBody StaffDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Staff updated", staffService.updateStaff(id, dto)));
    }
    @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        staffService.deleteStaff(id); return ResponseEntity.ok(ApiResponse.success("Staff deleted"));
    }
}
