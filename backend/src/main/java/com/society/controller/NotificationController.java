package com.society.controller;

import com.society.dto.ApiResponse;
import com.society.dto.NotificationDTO;
import com.society.security.JwtTokenProvider;
import com.society.service.NotificationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/notifications")
@RequiredArgsConstructor @Tag(name = "Notifications") @SecurityRequirement(name = "bearerAuth")
public class NotificationController {
    private final NotificationService notificationService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getMyNotifications(HttpServletRequest req) {
        Long userId = jwtTokenProvider.getUserIdFromRequest(req);
        return ResponseEntity.ok(ApiResponse.success("Notifications fetched", notificationService.getNotificationsForUser(userId)));
    }
    @PutMapping("/read/{id}")
    public ResponseEntity<ApiResponse<Void>> markRead(@PathVariable Long id, HttpServletRequest req) {
        notificationService.markAsRead(id, jwtTokenProvider.getUserIdFromRequest(req));
        return ResponseEntity.ok(ApiResponse.success("Marked as read"));
    }
    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllRead(HttpServletRequest req) {
        notificationService.markAllAsRead(jwtTokenProvider.getUserIdFromRequest(req));
        return ResponseEntity.ok(ApiResponse.success("All marked as read"));
    }
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> unreadCount(HttpServletRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Unread count", notificationService.getUnreadCount(jwtTokenProvider.getUserIdFromRequest(req))));
    }
}
