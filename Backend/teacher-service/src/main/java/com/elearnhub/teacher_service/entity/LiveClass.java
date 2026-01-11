package com.elearnhub.teacher_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "live_classes")
public class LiveClass {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long classId;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private LocalDateTime scheduledStartTime;
    
    @Column(nullable = false)
    private LocalDateTime scheduledEndTime;
    
    @Column
    private LocalDateTime actualStartTime;
    
    @Column
    private LocalDateTime actualEndTime;
    
    @Column(nullable = false)
    private String status = "SCHEDULED"; // SCHEDULED, LIVE, ENDED, CANCELLED
    
    @Column(unique = true)
    private String meetingId; // Unique identifier for the meeting room
    
    @Column
    private String meetingPassword;
    
    @Column(nullable = false)
    private Long hostId; // Teacher ID
    
    @Column
    private String recordingUrl;
    
    @Column(nullable = false)
    private Boolean allowRecording = false;
    
    @Column(nullable = false)
    private Boolean allowChat = true;
    
    @Column(nullable = false)
    private Boolean allowScreenShare = true;
    
    @Column(nullable = false)
    private Integer maxParticipants = 100;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime updatedAt;
    
    // Constructors
    public LiveClass() {
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getClassId() {
        return classId;
    }
    
    public void setClassId(Long classId) {
        this.classId = classId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDateTime getScheduledStartTime() {
        return scheduledStartTime;
    }
    
    public void setScheduledStartTime(LocalDateTime scheduledStartTime) {
        this.scheduledStartTime = scheduledStartTime;
    }
    
    public LocalDateTime getScheduledEndTime() {
        return scheduledEndTime;
    }
    
    public void setScheduledEndTime(LocalDateTime scheduledEndTime) {
        this.scheduledEndTime = scheduledEndTime;
    }
    
    public LocalDateTime getActualStartTime() {
        return actualStartTime;
    }
    
    public void setActualStartTime(LocalDateTime actualStartTime) {
        this.actualStartTime = actualStartTime;
    }
    
    public LocalDateTime getActualEndTime() {
        return actualEndTime;
    }
    
    public void setActualEndTime(LocalDateTime actualEndTime) {
        this.actualEndTime = actualEndTime;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getMeetingId() {
        return meetingId;
    }
    
    public void setMeetingId(String meetingId) {
        this.meetingId = meetingId;
    }
    
    public String getMeetingPassword() {
        return meetingPassword;
    }
    
    public void setMeetingPassword(String meetingPassword) {
        this.meetingPassword = meetingPassword;
    }
    
    public Long getHostId() {
        return hostId;
    }
    
    public void setHostId(Long hostId) {
        this.hostId = hostId;
    }
    
    public String getRecordingUrl() {
        return recordingUrl;
    }
    
    public void setRecordingUrl(String recordingUrl) {
        this.recordingUrl = recordingUrl;
    }
    
    public Boolean getAllowRecording() {
        return allowRecording;
    }
    
    public void setAllowRecording(Boolean allowRecording) {
        this.allowRecording = allowRecording;
    }
    
    public Boolean getAllowChat() {
        return allowChat;
    }
    
    public void setAllowChat(Boolean allowChat) {
        this.allowChat = allowChat;
    }
    
    public Boolean getAllowScreenShare() {
        return allowScreenShare;
    }
    
    public void setAllowScreenShare(Boolean allowScreenShare) {
        this.allowScreenShare = allowScreenShare;
    }
    
    public Integer getMaxParticipants() {
        return maxParticipants;
    }
    
    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
