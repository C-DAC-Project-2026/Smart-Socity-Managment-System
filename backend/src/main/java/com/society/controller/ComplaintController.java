package com.society.controller;

import com.society.dto.ApiResponse;
import com.society.dto.ComplaintDTO;
import com.society.dto.ComplaintStatusUpdateDTO;
import com.society.security.JwtTokenProvider;
import com.society.service.ComplaintService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/complaints")
@RequiredArgsConstructor @Tag(name = "Complaints") @SecurityRequirement(name = "bearerAuth")
public class ComplaintController {
    private final ComplaintService complaintService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping @PreAuthorize("hasRole('RESIDENT')")
    public ResponseEntity<ApiResponse<ComplaintDTO>> create(@Valid @RequestBody ComplaintDTO dto, HttpServletRequest req) {
        Long userId = jwtTokenProvider.getUserIdFromRequest(req);
        return ResponseEntity.ok(ApiResponse.success("Complaint raised", complaintService.createComplaint(dto, userId)));
    }
    @GetMapping @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<Page<ComplaintDTO>>> getAll(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Complaints fetched",
            complaintService.getAllComplaints(PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }
    @GetMapping("/resident/{residentId}") @PreAuthorize("hasAnyRole('ADMIN','RESIDENT')")
    public ResponseEntity<ApiResponse<Page<ComplaintDTO>>> getByResident(@PathVariable Long residentId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Complaints fetched",
            complaintService.getComplaintsByResident(residentId, PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }
    @GetMapping("/staff/{staffId}") @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<Page<ComplaintDTO>>> getByStaff(@PathVariable Long staffId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Complaints fetched",
            complaintService.getComplaintsByStaff(staffId, PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }
    @GetMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','RESIDENT','STAFF')")
    public ResponseEntity<ApiResponse<ComplaintDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Complaint fetched", complaintService.getComplaintById(id)));
    }
    @PutMapping("/status/{id}") @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<ComplaintDTO>> updateStatus(@PathVariable Long id,
            @Valid @RequestBody ComplaintStatusUpdateDTO dto, HttpServletRequest req) {
        Long userId = jwtTokenProvider.getUserIdFromRequest(req);
        return ResponseEntity.ok(ApiResponse.success("Status updated", complaintService.updateComplaintStatus(id, dto, userId)));
    }
    @PutMapping("/assign/{id}") @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ComplaintDTO>> assign(@PathVariable Long id,
            @RequestParam Long staffId, HttpServletRequest req) {
        Long userId = jwtTokenProvider.getUserIdFromRequest(req);
        return ResponseEntity.ok(ApiResponse.success("Complaint assigned", complaintService.assignComplaint(id, staffId, userId)));
    }
    @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        complaintService.deleteComplaint(id); return ResponseEntity.ok(ApiResponse.success("Complaint deleted"));
    }
}
