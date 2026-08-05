package com.society.controller;

import com.society.dto.ApiResponse;
import com.society.dto.NoticeDTO;
import com.society.security.JwtTokenProvider;
import com.society.service.NoticeService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/notices")
@RequiredArgsConstructor @Tag(name = "Notices") @SecurityRequirement(name = "bearerAuth")
public class NoticeController {
    private final NoticeService noticeService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<NoticeDTO>> create(@Valid @RequestBody NoticeDTO dto, HttpServletRequest req) {
        Long userId = jwtTokenProvider.getUserIdFromRequest(req);
        return ResponseEntity.ok(ApiResponse.success("Notice published", noticeService.createNotice(dto, userId)));
    }
    @GetMapping @PreAuthorize("hasAnyRole('ADMIN','RESIDENT','STAFF')")
    public ResponseEntity<ApiResponse<Page<NoticeDTO>>> getAll(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Notices fetched",
            noticeService.getAllNotices(PageRequest.of(page, size))));
    }
    @GetMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','RESIDENT','STAFF')")
    public ResponseEntity<ApiResponse<NoticeDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Notice fetched", noticeService.getNoticeById(id)));
    }
    @PutMapping("/{id}") @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<NoticeDTO>> update(@PathVariable Long id, @Valid @RequestBody NoticeDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Notice updated", noticeService.updateNotice(id, dto)));
    }
    @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        noticeService.deleteNotice(id); return ResponseEntity.ok(ApiResponse.success("Notice deleted"));
    }
}
