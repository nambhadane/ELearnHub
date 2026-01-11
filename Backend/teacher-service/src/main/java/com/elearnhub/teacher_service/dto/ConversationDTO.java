package com.elearnhub.teacher_service.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ConversationDTO {
    private Long id;
    private String type; // "DIRECT" or "GROUP"
    private String name;
    private Long classId;
    private List<ParticipantDTO> participants;
    private MessageDTO lastMessage;
    private int unreadCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Long getClassId() {
        return classId;
    }
    
    public void setClassId(Long classId) {
        this.classId = classId;
    }
    
    public List<ParticipantDTO> getParticipants() {
        return participants;
    }
    
    public void setParticipants(List<ParticipantDTO> participants) {
        this.participants = participants;
    }
    
    public MessageDTO getLastMessage() {
        return lastMessage;
    }
    
    public void setLastMessage(MessageDTO lastMessage) {
        this.lastMessage = lastMessage;
    }
    
    public int getUnreadCount() {
        return unreadCount;
    }
    
    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
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