package com.society.repository;
import com.society.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // Scoped by both userId AND societyId: defense-in-depth in case a userId
    // is ever guessed/enumerated, the society check still blocks cross-tenant reads.
    List<Notification> findByUser_UserIdAndSociety_SocietyIdOrderByCreatedAtDesc(Long userId, Long societyId);
    long countByUser_UserIdAndSociety_SocietyIdAndIsReadFalse(Long userId, Long societyId);
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.userId = :userId AND n.society.societyId = :societyId")
    void markAllReadByUserId(@Param("userId") Long userId, @Param("societyId") Long societyId);
}
