package com.elearnhub.teacher_service.repository;

import com.elearnhub.teacher_service.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Find all notifications for a user, ordered by newest first
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // Find unread notifications for a user
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.isRead = false ORDER BY n.createdAt DESC")
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(@Param("userId") Long userId);
    
    // Count unread notifications for a user
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.isRead = false")
    Long countByUserIdAndIsReadFalse(@Param("userId") Long userId);
    
    // Mark all notifications as read for a user
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.userId = :userId AND n.isRead = false")
    void markAllAsReadByUserId(@Param("userId") Long userId);
    
    // Delete old read notifications (cleanup)
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.userId = :userId AND n.isRead = true AND n.createdAt < :beforeDate")
    void deleteOldReadNotifications(@Param("userId") Long userId, @Param("beforeDate") java.time.LocalDateTime beforeDate);
}
