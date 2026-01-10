package com.elearnhub.teacher_service.service;

import com.elearnhub.teacher_service.dto.NotificationDTO;
import com.elearnhub.teacher_service.entity.Notification;

import java.util.List;

public interface NotificationService {
    
    // Create a new notification
    NotificationDTO createNotification(Long userId, String title, String message, 
                                      Notification.NotificationType type, 
                                      Long referenceId, String referenceType);
    
    // Get all notifications for a user
    List<NotificationDTO> getUserNotifications(Long userId);
    
    // Get unread notifications for a user
    List<NotificationDTO> getUnreadNotifications(Long userId);
    
    // Get unread count
    Long getUnreadCount(Long userId);
    
    // Mark notification as read
    NotificationDTO markAsRead(Long notificationId, Long userId);
    
    // Mark all notifications as read
    void markAllAsRead(Long userId);
    
    // Delete notification
    void deleteNotification(Long notificationId, Long userId);
    
    // Delete all read notifications
    void deleteAllRead(Long userId);
}
