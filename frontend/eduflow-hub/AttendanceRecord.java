package com.elearnhub.teacher_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_record", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"session_id", "student_id"}))
public class AttendanceRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private AttendanceSession session;
    
    @Column(name = "session_id", insertable = false, updatable = false)
    private Long sessionId;
    
    @Column(name = "student_id", nullable = false)
    private Long studentId;
    
    @Column(nullable = false)
    private String status; // PRESENT, ABSENT, LATE
    
    @Column(name = "marked_at", nullable = false)
    private LocalDateTime markedAt;
    
    @Column(name = "marked_by", nullable = false)
    private Long markedBy;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    // Constructors
    public AttendanceRecord() {
        this.markedAt = LocalDateTime.now();
        this.status = "ABSENT";
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public AttendanceSession getSession() {
        return session;
    }
    
    public void setSession(AttendanceSession session) {
        this.session = session;
    }
    
    public Long getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }
    
    public Long getStudentId() {
        return studentId;
    }
    
    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getMarkedAt() {
        return markedAt;
    }
    
    public void setMarkedAt(LocalDateTime markedAt) {
        this.markedAt = markedAt;
    }
    
    public Long getMarkedBy() {
        return markedBy;
    }
    
    public void setMarkedBy(Long markedBy) {
        this.markedBy = markedBy;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
}
