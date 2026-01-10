// ============================================
// FIXED MessageServiceImpl - Works with Lombok @Data
// ============================================

package com.elearnhub.teacher_service.service;

import com.elearnhub.teacher_service.dto.*;
import com.elearnhub.teacher_service.entity.*;
import com.elearnhub.teacher_service.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Override
    public List<ConversationDTO> getConversationsByUser(Long userId) {
        List<Conversation> conversations = conversationRepository.findByParticipantId(userId);
        
        return conversations.stream().map(conv -> {
            ConversationDTO dto = new ConversationDTO();
            dto.setId(conv.getId());
            dto.setType(conv.getType() != null ? conv.getType().name() : null);
            dto.setName(conv.getName());
            dto.setClassId(conv.getClassId());
            dto.setCreatedAt(conv.getCreatedAt());
            dto.setUpdatedAt(conv.getUpdatedAt());
            
            // Get participants
            if (conv.getParticipants() != null) {
                List<ParticipantDTO> participants = conv.getParticipants().stream()
                    .map(user -> {
                        ParticipantDTO p = new ParticipantDTO();
                        p.setId(user.getId());
                        p.setUsername(user.getUsername());
                        p.setName(user.getName());
                        p.setRole(user.getRole());
                        p.setAvatar(user.getProfilePicture());
                        return p;
                    })
                    .collect(Collectors.toList());
                dto.setParticipants(participants);
            }
            
            // Get last message
            List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtDesc(conv.getId());
            if (!messages.isEmpty()) {
                Message lastMsg = messages.get(0);
                MessageDTO msgDto = convertMessageToDTO(lastMsg);
                dto.setLastMessage(msgDto);
            }
            
            // Get unread count
            Optional<ConversationParticipant> participant = participantRepository
                .findByConversationIdAndUserId(conv.getId(), userId);
            if (participant.isPresent()) {
                ConversationParticipant cp = participant.get();
                dto.setUnreadCount(cp.getUnreadCount() != null ? cp.getUnreadCount() : 0);
            } else {
                dto.setUnreadCount(0);
            }
            
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<MessageDTO> getMessagesByConversation(Long conversationId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<Message> messages = messageRepository.findByConversationIdOrdered(conversationId, pageable);
        
        // Reverse to show oldest first
        List<Message> reversed = new ArrayList<>(messages);
        java.util.Collections.reverse(reversed);
        
        return reversed.stream()
            .map(this::convertMessageToDTO)
            .collect(Collectors.toList());
    }

    @Override
    public MessageDTO sendMessage(Long conversationId, Long senderId, String content) {
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
        
        // Add participants
        List<User> participantsList = new ArrayList<>();
        participantsList.add(user1);
        participantsList.add(user2);
        conversation.setParticipants(participantsList);
        
        conversation = conversationRepository.save(conversation);
        
        // Create participant records
        ConversationParticipant cp1 = new ConversationParticipant();
        cp1.setConversation(conversation);
        cp1.setUser(user1);
        cp1.setUnreadCount(0);
        participantRepository.save(cp1);
        
        ConversationParticipant cp2 = new ConversationParticipant();
        cp2.setConversation(conversation);
        cp2.setUser(user2);
        cp2.setUnreadCount(0);
        participantRepository.save(cp2);
        
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
        // Check if conversation already exists
        Optional<Conversation> existing = conversationRepository.findByClassId(classId);
        if (existing.isPresent()) {
            return convertConversationToDTO(existing.get(), null);
        }
        
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
        
        // Get all students in the class
        List<ParticipantDTO> students = classService.getClassStudents(classId);
        
        // Add teacher
        Optional<User> teacherOpt = userService.getUserById(classEntity.getTeacherId());
        if (teacherOpt.isEmpty()) {
            throw new RuntimeException("Teacher not found");
        }
        User teacher = teacherOpt.get();
        
        List<User> participantsList = new ArrayList<>();
        participantsList.add(teacher);
        
        // Add students
        for (ParticipantDTO student : students) {
            Optional<User> studentUserOpt = userService.getUserById(student.getId());
            if (studentUserOpt.isPresent()) {
                participantsList.add(studentUserOpt.get());
            }
        }
        
        conversation.setParticipants(participantsList);
        conversation = conversationRepository.save(conversation);
        
        // Create participant records
        for (User participant : participantsList) {
            ConversationParticipant cp = new ConversationParticipant();
            cp.setConversation(conversation);
            cp.setUser(participant);
            cp.setUnreadCount(0);
            participantRepository.save(cp);
        }
        
        return convertConversationToDTO(conversation, null);
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
        
        // Check if already participant
        if (participantRepository.existsByConversationIdAndUserId(conversationId, userId)) {
            return;
        }
        
        // Add to conversation
        if (conversation.getParticipants() == null) {
            conversation.setParticipants(new ArrayList<>());
        }
        conversation.getParticipants().add(user);
        conversationRepository.save(conversation);
        
        // Create participant record
        ConversationParticipant cp = new ConversationParticipant();
        cp.setConversation(conversation);
        cp.setUser(user);
        cp.setUnreadCount(0);
        participantRepository.save(cp);
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
        
        // Get participants
        if (conversation.getParticipants() != null) {
            List<ParticipantDTO> participants = conversation.getParticipants().stream()
                .map(user -> {
                    ParticipantDTO p = new ParticipantDTO();
                    p.setId(user.getId());
                    p.setUsername(user.getUsername());
                    p.setName(user.getName());
                    p.setRole(user.getRole());
                    p.setAvatar(user.getProfilePicture());
                    return p;
                })
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

// ============================================
// IMPORTANT: Make sure your Conversation entity has Lombok @Data
// If Lombok is not working, add explicit getters/setters:
// ============================================

/*
If you're getting "method getId() is undefined", it means Lombok @Data is not generating getters/setters.
Either:
1. Enable Lombok annotation processing in your IDE
2. Or add explicit getters/setters to your Conversation entity:

@Entity
@Table(name = "conversations")
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ConversationType type;
    
    // ... other fields
    
    // Explicit getters/setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public ConversationType getType() {
        return type;
    }
    
    public void setType(ConversationType type) {
        this.type = type;
    }
    
    // ... add getters/setters for all fields
}
*/

