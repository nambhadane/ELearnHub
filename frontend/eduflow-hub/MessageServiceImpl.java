// ============================================
// CORRECTED MessageServiceImpl - Fixed participant handling
// ============================================

package com.elearnhub.teacher_service.service;

import com.elearnhub.teacher_service.dto.*;
import com.elearnhub.teacher_service.entity.*;
import com.elearnhub.teacher_service.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class MessageServiceImpl implements MessageService {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ConversationParticipantRepository participantRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ClassService classService;

    @Autowired
    private NotificationService notificationService;

    @Override
    public List<ConversationDTO> getConversationsByUser(Long userId) {
        List<Conversation> conversations = conversationRepository.findByParticipantId(userId);
        
        return conversations.stream().map(conv -> {
            return convertConversationToDTO(conv, userId);
        }).collect(Collectors.toList());
    }

    @Override
    public List<MessageDTO> getMessagesByConversation(Long conversationId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        // ✅ FIX: Order ASC so oldest messages are first, newest at bottom (WhatsApp style)
        List<Message> messages = messageRepository.findByConversationIdOrdered(conversationId, pageable);
        
        // No need to reverse - messages are already ordered oldest to newest
        return messages.stream()
            .map(this::convertMessageToDTO)
            .collect(Collectors.toList());
    }

    @Override
    public MessageDTO sendMessage(Long conversationId, Long senderId, String content) {
        return sendMessage(conversationId, senderId, content, new ArrayList<>());
    }

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
        message.setCreatedAt(LocalDateTime.now());
        
        // Set file paths if provided
        if (filePaths != null && !filePaths.isEmpty()) {
            message.setFilePaths(String.join(",", filePaths));
        }
        
        message = messageRepository.save(message);
        
        // Update conversation updatedAt
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);
        
        // Increment unread count and create notifications for all participants except sender
        List<ConversationParticipant> participants = participantRepository
            .findByConversationId(conversationId);
        for (ConversationParticipant cp : participants) {
            if (cp.getUser() != null && !cp.getUser().getId().equals(senderId)) {
                // Increment unread count
                Integer currentCount = cp.getUnreadCount() != null ? cp.getUnreadCount() : 0;
                cp.setUnreadCount(currentCount + 1);
                participantRepository.save(cp);
                
                // Create notification
                String senderName = sender.getName() != null ? sender.getName() : sender.getUsername();
                String notificationTitle = "New Message";
                String notificationMessage = senderName + " sent you a message";
                
                // Add preview of message content if available
                if (content != null && !content.trim().isEmpty()) {
                    String preview = content.length() > 50 ? content.substring(0, 50) + "..." : content;
                    notificationMessage = senderName + ": " + preview;
                } else if (filePaths != null && !filePaths.isEmpty()) {
                    notificationMessage = senderName + " sent you a file";
                }
                
                // Use conversation ID as reference so we can navigate to it
                notificationService.createNotification(
                    cp.getUser().getId(),
                    notificationTitle,
                    notificationMessage,
                    Notification.NotificationType.MESSAGE,
                    conversationId,  // Store conversation ID for navigation
                    "CONVERSATION"   // Changed to CONVERSATION for clarity
                );
            }
        }
        
        return convertMessageToDTO(message);
    }

    @Override
    public ConversationDTO createDirectConversation(Long userId1, Long userId2) {
        // Check if conversation already exists
        Optional<Conversation> existing = conversationRepository
            .findDirectConversation(userId1, userId2);
        if (existing.isPresent()) {
            return convertConversationToDTO(existing.get(), userId1);
        }
        
        // Create new conversation
        Conversation conversation = new Conversation();
        conversation.setType(Conversation.ConversationType.DIRECT);
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setUpdatedAt(LocalDateTime.now());
        
        // Get users
        Optional<User> user1Opt = userService.getUserById(userId1);
        Optional<User> user2Opt = userService.getUserById(userId2);
        
        if (user1Opt.isEmpty()) {
            throw new RuntimeException("User 1 not found");
        }
        if (user2Opt.isEmpty()) {
            throw new RuntimeException("User 2 not found");
        }
        
        User user1 = user1Opt.get();
        User user2 = user2Opt.get();
        
        // ✅ FIX: Save conversation first, then create participant records
        conversation = conversationRepository.save(conversation);
        
        // ✅ FIX: Create participant records using ConversationParticipant entity
        ConversationParticipant cp1 = new ConversationParticipant();
        cp1.setConversation(conversation);
        cp1.setUser(user1);
        cp1.setUnreadCount(0);
        cp1.setLastReadAt(null);
        participantRepository.saveAndFlush(cp1);
        
        ConversationParticipant cp2 = new ConversationParticipant();
        cp2.setConversation(conversation);
        cp2.setUser(user2);
        cp2.setUnreadCount(0);
        cp2.setLastReadAt(null);
        participantRepository.saveAndFlush(cp2);
        
        return convertConversationToDTO(conversation, userId1);
    }

    @Override
    public ConversationDTO findDirectConversation(Long userId1, Long userId2) {
        Optional<Conversation> conversation = conversationRepository
            .findDirectConversation(userId1, userId2);
        return conversation.map(conv -> convertConversationToDTO(conv, userId1)).orElse(null);
    }

    @Override
    public ConversationDTO createClassConversation(Long classId) {
        System.out.println("🔄 MessageServiceImpl.createClassConversation called for classId: " + classId);
        
        // Check if conversation already exists
        Optional<Conversation> existing = conversationRepository.findByClassId(classId);
        if (existing.isPresent()) {
            System.out.println("✅ Class conversation already exists for classId: " + classId + ", returning existing conversation");
            return convertConversationToDTO(existing.get(), null);
        }
        
        System.out.println("🔄 No existing conversation found, creating new one for classId: " + classId);
        
        // Get class info - unwrap Optional
        Optional<ClassEntity> classOpt = classService.getClassById(classId);
        if (classOpt.isEmpty()) {
            throw new RuntimeException("Class not found");
        }
        ClassEntity classEntity = classOpt.get();
        
        // Create conversation
        Conversation conversation = new Conversation();
        conversation.setType(Conversation.ConversationType.GROUP);
        conversation.setName(classEntity.getName());
        conversation.setClassId(classId);
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setUpdatedAt(LocalDateTime.now());
        
        conversation = conversationRepository.save(conversation);
        System.out.println("✅ Conversation saved with ID: " + conversation.getId());
        
        // Get all students in the class
        List<ParticipantDTO> students = classService.getClassStudents(classId);
        System.out.println("📚 Found " + students.size() + " students in class " + classId);
        
        // ✅ FIX: Add teacher using ConversationParticipant entity
        // Get teacher ID from the teacher object, not the teacherId field (which may be null)
        User teacher = classEntity.getTeacher();
        if (teacher == null || teacher.getId() == null) {
            throw new RuntimeException("Teacher not found for class");
        }
        
        // ✅ FIX: Add teacher - check if already exists first
        if (!participantRepository.existsByConversationIdAndUserId(conversation.getId(), teacher.getId())) {
            ConversationParticipant teacherCp = new ConversationParticipant();
            teacherCp.setConversation(conversation);
            teacherCp.setUser(teacher);
            teacherCp.setUnreadCount(0);
            teacherCp.setLastReadAt(null);
            try {
                participantRepository.saveAndFlush(teacherCp);
            } catch (DataIntegrityViolationException e) {
                // Ignore duplicate key errors (race condition)
                System.err.println("Teacher already participant in conversation: " + e.getMessage());
            }
        }
        
        // ✅ FIX: Add students using ConversationParticipant entities - check if exists first
        for (ParticipantDTO student : students) {
            Optional<User> studentOpt = userService.getUserById(student.getId());
            if (studentOpt.isPresent()) {
                Long studentId = studentOpt.get().getId();
                // Check if already a participant before adding
                if (!participantRepository.existsByConversationIdAndUserId(conversation.getId(), studentId)) {
                    ConversationParticipant studentCp = new ConversationParticipant();
                    studentCp.setConversation(conversation);
                    studentCp.setUser(studentOpt.get());
                    studentCp.setUnreadCount(0);
                    studentCp.setLastReadAt(null);
                    try {
                        participantRepository.saveAndFlush(studentCp);
                    } catch (DataIntegrityViolationException e) {
                        // Ignore duplicate key errors (race condition)
                        // Participant already exists, which is fine
                        System.err.println("Student already participant in conversation: " + e.getMessage());
                    }
                }
            }
        }
        
        System.out.println("✅ Class conversation creation completed for classId: " + classId);
        ConversationDTO result = convertConversationToDTO(conversation, null);
        System.out.println("📤 Returning conversation DTO with ID: " + result.getId() + ", Name: " + result.getName());
        return result;
    }

    @Override
    public ConversationDTO getClassConversation(Long classId) {
        Optional<Conversation> conversation = conversationRepository.findByClassId(classId);
        if (conversation.isPresent()) {
            return convertConversationToDTO(conversation.get(), null);
        }
        // If doesn't exist, create it
        return createClassConversation(classId);
    }

    @Override
    public boolean isUserParticipant(Long conversationId, Long userId) {
        return participantRepository.existsByConversationIdAndUserId(conversationId, userId);
    }

    @Override
    public void markMessagesAsRead(Long conversationId, Long userId) {
        Optional<ConversationParticipant> participant = participantRepository
            .findByConversationIdAndUserId(conversationId, userId);
        if (participant.isPresent()) {
            ConversationParticipant cp = participant.get();
            cp.setUnreadCount(0);
            cp.setLastReadAt(LocalDateTime.now());
            participantRepository.save(cp);
        }
    }

    @Override
    public void addParticipantToConversation(Long conversationId, Long userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new RuntimeException("Conversation not found"));
        
        Optional<User> userOpt = userService.getUserById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        User user = userOpt.get();
        
        // ✅ FIX: Check if already participant - return silently if exists
        if (participantRepository.existsByConversationIdAndUserId(conversationId, userId)) {
            return; // Already a participant, no need to add again
        }
        
        // ✅ FIX: Create participant record directly, don't use setParticipants()
        try {
            ConversationParticipant cp = new ConversationParticipant();
            cp.setConversation(conversation);
            cp.setUser(user);
            cp.setUnreadCount(0);
            cp.setLastReadAt(null);
            participantRepository.saveAndFlush(cp);
        } catch (DataIntegrityViolationException e) {
            // If duplicate key error occurs (race condition), just ignore it
            // The participant already exists, which is what we want
            return;
        }
    }

    @Override
    public void removeParticipantFromConversation(Long conversationId, Long userId) {
        Optional<ConversationParticipant> participant = participantRepository
            .findByConversationIdAndUserId(conversationId, userId);
        if (participant.isPresent()) {
            participantRepository.delete(participant.get());
        }
    }
    
    // Helper methods
    private MessageDTO convertMessageToDTO(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        
        if (message.getConversation() != null) {
            dto.setConversationId(message.getConversation().getId());
        }
        
        if (message.getSender() != null) {
            dto.setSenderId(message.getSender().getId());
            dto.setSenderName(message.getSender().getName() != null ? 
                message.getSender().getName() : message.getSender().getUsername());
        }
        
        dto.setContent(message.getContent());
        dto.setCreatedAt(message.getCreatedAt());
        dto.setIsRead(message.getReadAt() != null);
        
        // Convert file paths to attachments
        if (message.getFilePaths() != null && !message.getFilePaths().isEmpty()) {
            String[] paths = message.getFilePaths().split(",");
            List<MessageAttachmentDTO> attachments = new ArrayList<>();
            
            for (String path : paths) {
                if (path != null && !path.trim().isEmpty()) {
                    MessageAttachmentDTO attachment = new MessageAttachmentDTO();
                    
                    // Extract filename from path
                    String filename = path.substring(path.lastIndexOf("/") + 1);
                    attachment.setName(filename);
                    
                    // Create download URL - use the filename from the path
                    attachment.setUrl("/api/messages/files/" + filename);
                    
                    // Determine file type from extension
                    String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
                    if (extension.matches("jpg|jpeg|png|gif|bmp|webp")) {
                        attachment.setType("image/" + extension);
                    } else if (extension.matches("pdf")) {
                        attachment.setType("application/pdf");
                    } else if (extension.matches("doc|docx")) {
                        attachment.setType("application/msword");
                    } else {
                        attachment.setType("application/octet-stream");
                    }
                    
                    attachments.add(attachment);
                }
            }
            
            dto.setAttachments(attachments);
        }
        
        return dto;
    }
    
    private ConversationDTO convertConversationToDTO(Conversation conversation, Long currentUserId) {
        ConversationDTO dto = new ConversationDTO();
        dto.setId(conversation.getId());
        dto.setType(conversation.getType() != null ? conversation.getType().name() : null);
        dto.setName(conversation.getName());
        dto.setClassId(conversation.getClassId());
        dto.setCreatedAt(conversation.getCreatedAt());
        dto.setUpdatedAt(conversation.getUpdatedAt());
        
        // ✅ FIX: Get participants from ConversationParticipant entities
        List<ConversationParticipant> participantEntities = participantRepository
            .findByConversationId(conversation.getId());
        
        if (participantEntities != null && !participantEntities.isEmpty()) {
            List<ParticipantDTO> participants = participantEntities.stream()
                .map(cp -> {
                    User user = cp.getUser();
                    if (user == null) return null;
                    ParticipantDTO p = new ParticipantDTO();
                    p.setId(user.getId());
                    p.setUsername(user.getUsername());
                    p.setName(user.getName());
                    p.setRole(user.getRole());
                    p.setAvatar(user.getProfilePicture());
                    return p;
                })
                .filter(p -> p != null)
                .collect(Collectors.toList());
            dto.setParticipants(participants);
        }
        
        // Get last message
        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtDesc(conversation.getId());
        if (!messages.isEmpty()) {
            Message lastMsg = messages.get(0);
            MessageDTO msgDto = convertMessageToDTO(lastMsg);
            dto.setLastMessage(msgDto);
        }
        
        // Get unread count
        if (currentUserId != null) {
            Optional<ConversationParticipant> participant = participantRepository
                .findByConversationIdAndUserId(conversation.getId(), currentUserId);
            if (participant.isPresent()) {
                ConversationParticipant cp = participant.get();
                dto.setUnreadCount(cp.getUnreadCount() != null ? cp.getUnreadCount() : 0);
            } else {
                dto.setUnreadCount(0);
            }
        }
        
        return dto;
    }
}
