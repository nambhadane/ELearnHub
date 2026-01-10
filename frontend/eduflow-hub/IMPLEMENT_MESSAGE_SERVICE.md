# MessageService Implementation Guide

Based on your backend code, here's the complete implementation for `MessageServiceImpl`:

## Required Repositories

First, create these repository interfaces:

```java
package com.elearnhub.teacher_service.repository;

import com.elearnhub.teacher_service.entity.Conversation;
import com.elearnhub.teacher_service.entity.Message;
import com.elearnhub.teacher_service.entity.ConversationParticipant;
import com.elearnhub.teacher_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    @Query("SELECT DISTINCT c FROM Conversation c " +
           "JOIN c.participants p WHERE p.id = :userId " +
           "ORDER BY c.updatedAt DESC")
    List<Conversation> findByParticipantId(@Param("userId") Long userId);
    
    Optional<Conversation> findByClassId(Long classId);
    
    @Query("SELECT c FROM Conversation c " +
           "JOIN c.participants p1 JOIN c.participants p2 " +
           "WHERE c.type = 'DIRECT' " +
           "AND p1.id = :userId1 AND p2.id = :userId2")
    Optional<Conversation> findDirectConversation(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversationIdOrderByCreatedAtDesc(Long conversationId);
    
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId ORDER BY m.createdAt DESC")
    List<Message> findByConversationIdOrdered(@Param("conversationId") Long conversationId, 
                                               org.springframework.data.domain.Pageable pageable);
}

@Repository
public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, Long> {
    Optional<ConversationParticipant> findByConversationIdAndUserId(Long conversationId, Long userId);
    
    List<ConversationParticipant> findByConversationId(Long conversationId);
    
    boolean existsByConversationIdAndUserId(Long conversationId, Long userId);
}
```

## Complete MessageServiceImpl Implementation

```java
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
            dto.setType(conv.getType().name());
            dto.setName(conv.getName());
            dto.setClassId(conv.getClassId());
            dto.setCreatedAt(conv.getCreatedAt());
            dto.setUpdatedAt(conv.getUpdatedAt());
            
            // Get participants
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
                dto.setUnreadCount(participant.get().getUnreadCount());
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
        
        return messages.stream()
            .map(this::convertMessageToDTO)
            .collect(Collectors.toList());
    }

    @Override
    public MessageDTO sendMessage(Long conversationId, Long senderId, String content) {
        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new RuntimeException("Conversation not found"));
        
        User sender = userService.getUserById(senderId)
            .orElseThrow(() -> new RuntimeException("Sender not found"));
        
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
            if (!cp.getUser().getId().equals(senderId)) {
                cp.setUnreadCount(cp.getUnreadCount() + 1);
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
        User user1 = userService.getUserById(userId1)
            .orElseThrow(() -> new RuntimeException("User 1 not found"));
        User user2 = userService.getUserById(userId2)
            .orElseThrow(() -> new RuntimeException("User 2 not found"));
        
        // Add participants
        conversation.setParticipants(new ArrayList<>());
        conversation.getParticipants().add(user1);
        conversation.getParticipants().add(user2);
        
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
        
        // Get class info
        ClassDTO classDTO = classService.getClassById(classId);
        if (classDTO == null) {
            throw new RuntimeException("Class not found");
        }
        
        // Create conversation
        Conversation conversation = new Conversation();
        conversation.setType(Conversation.ConversationType.GROUP);
        conversation.setName(classDTO.getName());
        conversation.setClassId(classId);
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setUpdatedAt(LocalDateTime.now());
        
        conversation = conversationRepository.save(conversation);
        
        // Get all students in the class
        List<ParticipantDTO> students = classService.getClassStudents(classId);
        
        // Add teacher
        User teacher = userService.getUserById(classDTO.getTeacherId())
            .orElseThrow(() -> new RuntimeException("Teacher not found"));
        
        conversation.setParticipants(new ArrayList<>());
        conversation.getParticipants().add(teacher);
        
        // Add students
        for (ParticipantDTO student : students) {
            User studentUser = userService.getUserById(student.getId())
                .orElseThrow(() -> new RuntimeException("Student not found"));
            conversation.getParticipants().add(studentUser);
        }
        
        conversation = conversationRepository.save(conversation);
        
        // Create participant records
        for (User participant : conversation.getParticipants()) {
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
        return conversation.map(conv -> convertConversationToDTO(conv, null)).orElse(null);
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
        
        User user = userService.getUserById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if already participant
        if (participantRepository.existsByConversationIdAndUserId(conversationId, userId)) {
            return;
        }
        
        // Add to conversation
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
        dto.setConversationId(message.getConversation().getId());
        dto.setSenderId(message.getSender().getId());
        dto.setSenderName(message.getSender().getName() != null ? 
            message.getSender().getName() : message.getSender().getUsername());
        dto.setContent(message.getContent());
        dto.setCreatedAt(message.getCreatedAt());
        dto.setIsRead(message.getReadAt() != null);
        return dto;
    }
    
    private ConversationDTO convertConversationToDTO(Conversation conversation, Long currentUserId) {
        ConversationDTO dto = new ConversationDTO();
        dto.setId(conversation.getId());
        dto.setType(conversation.getType().name());
        dto.setName(conversation.getName());
        dto.setClassId(conversation.getClassId());
        dto.setCreatedAt(conversation.getCreatedAt());
        dto.setUpdatedAt(conversation.getUpdatedAt());
        
        // Get participants
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
                dto.setUnreadCount(participant.get().getUnreadCount());
            } else {
                dto.setUnreadCount(0);
            }
        }
        
        return dto;
    }
}
```

## Required Methods in UserService

Make sure `UserService` has this method:

```java
Optional<User> getUserById(Long userId);
```

## Required Methods in ClassService

Make sure `ClassService` has these methods:

```java
ClassDTO getClassById(Long classId);
List<ParticipantDTO> getClassStudents(Long classId);
```

## Notes

1. The `ConversationParticipant` entity uses a separate table, but the `Conversation` entity also has a `@ManyToMany` relationship. You may need to adjust based on your actual database schema.

2. If `ConversationParticipant` is just a join table, you might not need the separate entity and can use the `@ManyToMany` relationship directly.

3. Make sure all the imports are correct for your package structure.

4. The `getUserById` method should exist in `UserService` - if it doesn't, add it.

