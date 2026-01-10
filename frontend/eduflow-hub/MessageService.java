package com.elearnhub.teacher_service.service;

import com.elearnhub.teacher_service.dto.ConversationDTO;
import com.elearnhub.teacher_service.dto.MessageDTO;

import java.util.List;

public interface MessageService {
    
    // Get all conversations for a user
    List<ConversationDTO> getConversationsByUser(Long userId);
    
    // Get messages in a conversation
    List<MessageDTO> getMessagesByConversation(Long conversationId, int page, int size);
    
    // Send a message
    MessageDTO sendMessage(Long conversationId, Long senderId, String content);
    
    // Send a message with file attachments
    MessageDTO sendMessage(Long conversationId, Long senderId, String content, List<String> filePaths);
    
    // Create direct conversation between two users
    ConversationDTO createDirectConversation(Long userId1, Long userId2);
    
    // Find direct conversation between two users
    ConversationDTO findDirectConversation(Long userId1, Long userId2);
    
    // Create class group conversation
    ConversationDTO createClassConversation(Long classId);
    
    // Get class conversation
    ConversationDTO getClassConversation(Long classId);
    
    // Check if user is participant in conversation
    boolean isUserParticipant(Long conversationId, Long userId);
    
    // Mark messages as read
    void markMessagesAsRead(Long conversationId, Long userId);
    
    // Add participant to conversation
    void addParticipantToConversation(Long conversationId, Long userId);
    
    // Remove participant from conversation
    void removeParticipantFromConversation(Long conversationId, Long userId);
}