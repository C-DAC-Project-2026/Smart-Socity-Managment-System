package com.society.repository;
import com.society.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUser_UserIdOrderByCreatedAtDesc(Long userId);
    long countByUser_UserIdAndIsReadFalse(Long userId);
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.userId = :userId")
    void markAllReadByUserId(Long userId);
}
