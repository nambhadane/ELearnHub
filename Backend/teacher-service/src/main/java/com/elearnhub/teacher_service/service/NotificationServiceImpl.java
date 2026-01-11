package com.elearnhub.teacher_service.service;

import com.elearnhub.teacher_service.dto.NotificationDTO;
import com.elearnhub.teacher_service.entity.Notification;
import com.elearnhub.teacher_service.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    public NotificationDTO createNotification(Long userId, String title, String message,
                                             Notification.NotificationType type,
                                             Long referenceId, String referenceType) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setReferenceId(referenceId);
        notification.setReferenceType(referenceType);
        notification.setIsRead(false);
        
        Notification saved = notificationRepository.save(notification);
        return NotificationDTO.fromEntity(saved);
    }
    
    @Override
    public List<NotificationDTO> getUserNotifications(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return notifications.stream()
                .map(NotificationDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<NotificationDTO> getUnreadNotifications(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        return notifications.stream()
                .map(NotificationDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public Long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }
    
    @Override
    public NotificationDTO markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to notification");
        }
        
        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        
        Notification updated = notificationRepository.save(notification);
        return NotificationDTO.fromEntity(updated);
    }
    
    @Override
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }
    
    @Override
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to notification");
        }
        
        notificationRepository.delete(notification);
    }
    
    @Override
    public void deleteAllRead(Long userId) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        notificationRepository.deleteOldReadNotifications(userId, thirtyDaysAgo);
    }

	public NotificationDTO createNotification(Long userId, String title, String message, String string,
			Long referenceId, String referenceType) {
		// TODO Auto-generated method stub
		return null;
	}
}
