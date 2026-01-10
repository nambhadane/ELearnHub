// ============================================
// Updated MessageService Interface - Add File Support
// ============================================

package com.elearnhub.teacher_service.service;

import com.elearnhub.teacher_service.dto.*;
import java.util.List;

public interface MessageService {
    // ... existing methods ...
    
    // ✅ UPDATED: Add file paths parameter
    MessageDTO sendMessage(Long conversationId, Long senderId, String content, List<String> filePaths);
    
    // Keep old method for backward compatibility (can delegate to new one)
    default MessageDTO sendMessage(Long conversationId, Long senderId, String content) {
        return sendMessage(conversationId, senderId, content, null);
    }
    
    // ... rest of methods ...
}

// ============================================
// Updated MessageServiceImpl - Add File Support
// ============================================

@Override
public MessageDTO sendMessage(Long conversationId, Long senderId, String content, List<String> filePaths) {
    Conversation conversation = conversationRepository.findById(conversationId)
        .orElseThrow(() -> new RuntimeException("Conversation not found"));
    
    Optional<User> senderOpt = userService.getUserById(senderId);
    if (senderOpt.isEmpty()) {
        throw new RuntimeException("Sender not found");
    }
    User sender = senderOpt.get();
    
    Message message = new Message();
    message.setConversation(conversation);
    message.setSender(sender);
    message.setContent(content);
    
    // ✅ ADD: Set file paths (comma-separated)
    if (filePaths != null && !filePaths.isEmpty()) {
        message.setFilePaths(String.join(",", filePaths));
    }
    
    message.setCreatedAt(LocalDateTime.now());
    message = messageRepository.save(message);
    
    // Update conversation updatedAt
    conversation.setUpdatedAt(LocalDateTime.now());
    conversationRepository.save(conversation);
    
    // Increment unread count for all participants except sender
    List<ConversationParticipant> participants = participantRepository
        .findByConversationId(conversationId);
    for (ConversationParticipant cp : participants) {
        if (cp.getUser() != null && !cp.getUser().getId().equals(senderId)) {
            Integer currentCount = cp.getUnreadCount() != null ? cp.getUnreadCount() : 0;
            cp.setUnreadCount(currentCount + 1);
            participantRepository.save(cp);
        }
    }
    
    return convertMessageToDTO(message);
}

// ✅ UPDATED: convertMessageToDTO to include attachments
private MessageDTO convertMessageToDTO(Message message) {
    MessageDTO dto = new MessageDTO();
    dto.setId(message.getId());
    dto.setConversationId(message.getConversation().getId());
    dto.setSenderId(message.getSender().getId());
    dto.setSenderName(message.getSender().getName());
    dto.setContent(message.getContent());
    dto.setCreatedAt(message.getCreatedAt());
    dto.setIsRead(message.getReadAt() != null);
    
    // ✅ ADD: Parse file paths and create attachment DTOs
    if (message.getFilePaths() != null && !message.getFilePaths().isEmpty()) {
        List<MessageAttachmentDTO> attachments = new ArrayList<>();
        String[] paths = message.getFilePaths().split(",");
        for (String path : paths) {
            if (!path.trim().isEmpty()) {
                MessageAttachmentDTO attachment = new MessageAttachmentDTO();
                // Extract filename from path
                String filename = path.substring(path.lastIndexOf("/") + 1);
                attachment.setName(filename);
                attachment.setUrl("/api/messages/files/" + filename);
                attachments.add(attachment);
            }
        }
        dto.setAttachments(attachments);
    }
    
    return dto;
}

// ✅ ADD: MessageAttachmentDTO
package com.elearnhub.teacher_service.dto;

public class MessageAttachmentDTO {
    private Long id;
    private String name;
    private String url;
    private String type;
    private Long size;
    
    // Getters and setters
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

// ✅ UPDATED: MessageDTO to include attachments
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
    private List<MessageAttachmentDTO> attachments; // ✅ ADD
    
    // ... existing getters/setters ...
    
    public List<MessageAttachmentDTO> getAttachments() { return attachments; }
    public void setAttachments(List<MessageAttachmentDTO> attachments) { this.attachments = attachments; }
}

