// ============================================
// FIXED MessageDTO - Add attachments field
// ============================================

package com.elearnhub.teacher_service.dto;

import java.time.LocalDateTime;
import java.util.List;

public class MessageDTO {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private String content;
    private LocalDateTime createdAt;
    private Boolean isRead;
    
    // ✅ ADD: Attachments list (not filePaths)
    private List<MessageAttachmentDTO> attachments;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }

    // ✅ ADD: Attachments getter/setter
    public List<MessageAttachmentDTO> getAttachments() { return attachments; }
    public void setAttachments(List<MessageAttachmentDTO> attachments) { this.attachments = attachments; }
}

// ============================================
// MessageAttachmentDTO - New DTO for file attachments
// ============================================

package com.elearnhub.teacher_service.dto;

public class MessageAttachmentDTO {
    private Long id;
    private String name;
    private String url;
    private String type;
    private Long size;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }
}

