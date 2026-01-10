package com.elearnhub.teacher_service.dto;

import java.time.LocalDateTime;

public class LiveClassDTO {
    private Long id;
    private Long classId;
    private String className;
    private String title;
    private String description;
    private LocalDateTime scheduledStartTime;
    private LocalDateTime scheduledEndTime;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;
    private String status;
    private String meetingId;
    private String meetingPassword;
    private Long hostId;
    private String hostName;
    private String recordingUrl;
    private Boolean allowRecording;
    private Boolean allowChat;
    private Boolean allowScreenShare;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private LocalDateTime createdAt;
    
    // Constructors
    public LiveClassDTO() {}
    
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
    
    public String getClassName() {
        return className;
    }
    
    public void setClassName(String className) {
        this.className = className;
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
    
    public String getHostName() {
        return hostName;
    }
    
    public void setHostName(String hostName) {
        this.hostName = hostName;
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
    
    public Integer getCurrentParticipants() {
        return currentParticipants;
    }
    
    public void setCurrentParticipants(Integer currentParticipants) {
        this.currentParticipants = currentParticipants;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
