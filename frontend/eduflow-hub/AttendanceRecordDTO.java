package com.elearnhub.teacher_service.dto;

import java.time.LocalDateTime;

public class AttendanceRecordDTO {
    
    private Long id;
    private Long sessionId;
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private String status; // PRESENT, ABSENT, LATE
    private LocalDateTime markedAt;
    private Long markedBy;
    private String markedByName;
    private String notes;
    
    // Constructors
    public AttendanceRecordDTO() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
    
    public String getStudentName() {
        return studentName;
    }
    
    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }
    
    public String getStudentEmail() {
        return studentEmail;
    }
    
    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
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
    
    public String getMarkedByName() {
        return markedByName;
    }
    
    public void setMarkedByName(String markedByName) {
        this.markedByName = markedByName;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
}
