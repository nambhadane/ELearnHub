package com.elearnhub.teacher_service.dto;

import java.time.LocalDateTime;
import java.util.List;

public class MessageDTO {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private String content;
    private List<MessageAttachmentDTO> attachments;
    private boolean isRead;
    private LocalDateTime createdAt;
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getConversationId() {
        return conversationId;
    }
    
    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }
    
    public Long getSenderId() {
        return senderId;
    }
    
    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }
    
    public String getSenderName() {
        return senderName;
    }
    
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public List<MessageAttachmentDTO> getAttachments() {
        return attachments;
    }
    
    public void setAttachments(List<MessageAttachmentDTO> attachments) {
        this.attachments = attachments;
    }
    
    public boolean getIsRead() {
        return isRead;
    }
    
    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}