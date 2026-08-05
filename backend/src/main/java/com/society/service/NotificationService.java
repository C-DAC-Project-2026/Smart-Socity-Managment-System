package com.society.service;
import com.society.dto.NotificationDTO;
import java.util.List;
public interface NotificationService {
    List<NotificationDTO> getNotificationsForUser(Long userId);
    void markAsRead(Long notificationId, Long userId);
    void markAllAsRead(Long userId);
    void sendNotification(Long userId, String message, String type);
    long getUnreadCount(Long userId);
}
