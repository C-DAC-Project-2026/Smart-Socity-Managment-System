package com.society.serviceimpl;

import com.society.dto.NotificationDTO;
import com.society.entity.Notification;
import com.society.entity.User;
import com.society.exception.ForbiddenException;
import com.society.exception.ResourceNotFoundException;
import com.society.repository.NotificationRepository;
import com.society.repository.UserRepository;
import com.society.security.SecurityUtils;
import com.society.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    @Override public List<NotificationDTO> getNotificationsForUser(Long userId) {
        return notificationRepository
            .findByUser_UserIdAndSociety_SocietyIdOrderByCreatedAtDesc(userId, securityUtils.getCurrentSocietyId())
            .stream().map(this::toDTO).collect(Collectors.toList());
    }
    @Override @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification n = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));
        if (!n.getUser().getUserId().equals(userId)) throw new ForbiddenException("Access denied");
        n.setIsRead(true); notificationRepository.save(n);
    }
    @Override @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllReadByUserId(userId, securityUtils.getCurrentSocietyId());
    }
    @Override @Transactional
    public void sendNotification(Long userId, String message, String type) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getSociety() == null) return;
        Notification n = Notification.builder().message(message)
            .type(Notification.NotificationType.valueOf(type)).isRead(false)
            .user(user).society(user.getSociety()).build();
        notificationRepository.save(n);
    }
    @Override public long getUnreadCount(Long userId) {
        return notificationRepository.countByUser_UserIdAndSociety_SocietyIdAndIsReadFalse(userId, securityUtils.getCurrentSocietyId());
    }
    private NotificationDTO toDTO(Notification n) {
        return NotificationDTO.builder().notificationId(n.getNotificationId())
            .message(n.getMessage()).type(n.getType().name())
            .isRead(n.getIsRead()).createdAt(n.getCreatedAt()).build();
    }
}
