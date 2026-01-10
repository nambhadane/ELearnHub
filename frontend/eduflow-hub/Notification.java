package com.elearnhub.teacher_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String message;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;
    
    @Column(name = "reference_id")
    private Long referenceId; // ID of related entity (message, assignment, etc.)
    
    @Column(name = "reference_type")
    private String referenceType; // "MESSAGE", "ASSIGNMENT", "GRADE", etc.
    
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "read_at")
    private LocalDateTime readAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public enum NotificationType {
        MESSAGE,        // New message received
        ASSIGNMENT,     // New assignment posted
        GRADE,          // Assignment graded
        ANNOUNCEMENT,   // Class announcement
        ENROLLMENT,     // Enrolled in class
        SUBMISSION,     // Student submitted assignment
        REMINDER,       // Deadline reminder
        SYSTEM          // System notification
    }
}
