package com.elearnhub.teacher_service.Controller;

import com.elearnhub.teacher_service.dto.NotificationDTO;
import com.elearnhub.teacher_service.entity.User;
import com.elearnhub.teacher_service.service.NotificationService;
import com.elearnhub.teacher_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserService userService;
    
    // Get all notifications for current user
    @GetMapping
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<?> getNotifications(Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            List<NotificationDTO> notifications = notificationService.getUserNotifications(user.getId());
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return error("Failed to fetch notifications: " + e.getMessage());
        }
    }
    
    // Get unread notifications
    @GetMapping("/unread")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<?> getUnreadNotifications(Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            List<NotificationDTO> notifications = notificationService.getUnreadNotifications(user.getId());
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return error("Failed to fetch unread notifications: " + e.getMessage());
        }
    }
    
    // Get unread count
    @GetMapping("/unread/count")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<?> getUnreadCount(Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Long count = notificationService.getUnreadCount(user.getId());
            Map<String, Long> response = new HashMap<>();
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return error("Failed to fetch unread count: " + e.getMessage());
        }
    }
    
    // Mark notification as read
    @PutMapping("/{notificationId}/read")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<?> markAsRead(@PathVariable Long notificationId, 
                                       Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            NotificationDTO notification = notificationService.markAsRead(notificationId, user.getId());
            return ResponseEntity.ok(notification);
        } catch (Exception e) {
            return error("Failed to mark notification as read: " + e.getMessage());
        }
    }
    
    // Mark all notifications as read
    @PutMapping("/read-all")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<?> markAllAsRead(Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            notificationService.markAllAsRead(user.getId());
            Map<String, String> response = new HashMap<>();
            response.put("message", "All notifications marked as read");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return error("Failed to mark all as read: " + e.getMessage());
        }
    }
    
    // Delete notification
    @DeleteMapping("/{notificationId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<?> deleteNotification(@PathVariable Long notificationId,
                                               Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            notificationService.deleteNotification(notificationId, user.getId());
            Map<String, String> response = new HashMap<>();
            response.put("message", "Notification deleted");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return error("Failed to delete notification: " + e.getMessage());
        }
    }
    
    // Delete all read notifications
    @DeleteMapping("/read")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<?> deleteAllRead(Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            notificationService.deleteAllRead(user.getId());
            Map<String, String> response = new HashMap<>();
            response.put("message", "All read notifications deleted");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return error("Failed to delete read notifications: " + e.getMessage());
        }
    }
    
    private ResponseEntity<?> error(String msg) {
        Map<String, String> error = new HashMap<>();
        error.put("message", msg);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
