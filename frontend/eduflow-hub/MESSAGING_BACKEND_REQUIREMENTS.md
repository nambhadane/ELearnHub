# Messaging System - Backend Requirements

## Overview
The messaging system supports:
1. **1-on-1 Chats**: Teacher ↔ Student, Teacher ↔ Teacher
2. **Group Chats**: Class-based group chats (auto-created when class is created)
3. **Auto-enrollment**: Students automatically added to class group when enrolled

---

## Database Schema

### 1. Conversation Entity

```java
package com.elearnhub.teacher_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "conversations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ConversationType type; // DIRECT or GROUP

    @Column(name = "name")
    private String name; // For group chats (e.g., "Web Development - Section A")

    @Column(name = "class_id")
    private Long classId; // For class-based group chats

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "conversation_participants",
        joinColumns = @JoinColumn(name = "conversation_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> participants = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages = new ArrayList<>();

    public enum ConversationType {
        DIRECT,  // 1-on-1 chat
        GROUP    // Group chat (class-based or custom)
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

### 2. Message Entity

```java
package com.elearnhub.teacher_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(name = "content", nullable = false, length = 2000)
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

### 3. ConversationParticipant Entity (Optional - for read receipts)

```java
package com.elearnhub.teacher_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "conversation_participants")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "last_read_at")
    private LocalDateTime lastReadAt;

    @Column(name = "unread_count", nullable = false)
    private Integer unreadCount = 0;
}
```

---

## DTOs

### 1. ConversationDTO

```java
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer unreadCount;

    // Getters and setters...
}
```

### 2. MessageDTO

```java
package com.elearnhub.teacher_service.dto;

import java.time.LocalDateTime;

public class MessageDTO {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private String content;
    private LocalDateTime createdAt;
    private Boolean isRead;

    // Getters and setters...
}
```

### 3. ParticipantDTO

```java
package com.elearnhub.teacher_service.dto;

public class ParticipantDTO {
    private Long id;
    private String name;
    private String username;
    private String role;
    private String avatar;

    // Getters and setters...
}
```

### 4. CreateMessageRequest

```java
package com.elearnhub.teacher_service.dto;

public class CreateMessageRequest {
    private Long conversationId;
    private String content;

    // Getters and setters...
}
```

### 5. CreateConversationRequest

```java
package com.elearnhub.teacher_service.dto;

import java.util.List;

public class CreateConversationRequest {
    private String type; // "DIRECT" or "GROUP"
    private String name; // For group chats
    private Long classId; // For class-based groups
    private List<Long> participantIds; // For direct chats or custom groups

    // Getters and setters...
}
```

---

## Required Endpoints

### 1. Get All Conversations (for current user)

**Endpoint:** `GET /messages/conversations`  
**Authorization:** `@PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")`  
**Response:** `List<ConversationDTO>`

**Implementation:**
```java
@GetMapping("/conversations")
@PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
public ResponseEntity<?> getConversations(Authentication authentication) {
    try {
        String username = authentication.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<ConversationDTO> conversations = messageService.getConversationsByUser(user.getId());
        return ResponseEntity.ok(conversations);
    } catch (Exception e) {
        Map<String, String> error = new HashMap<>();
        error.put("message", "Failed to fetch conversations: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

---

### 2. Get Messages for a Conversation

**Endpoint:** `GET /messages/conversations/{conversationId}/messages`  
**Authorization:** `@PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")`  
**Query Params:** `?page=0&size=50` (optional pagination)  
**Response:** `List<MessageDTO>`

**Implementation:**
```java
@GetMapping("/conversations/{conversationId}/messages")
@PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
public ResponseEntity<?> getMessages(
        @PathVariable Long conversationId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size,
        Authentication authentication) {
    try {
        String username = authentication.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify user is participant
        if (!messageService.isUserParticipant(conversationId, user.getId())) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Unauthorized: You are not a participant in this conversation");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        List<MessageDTO> messages = messageService.getMessagesByConversation(conversationId, page, size);
        
        // Mark messages as read
        messageService.markMessagesAsRead(conversationId, user.getId());

        return ResponseEntity.ok(messages);
    } catch (Exception e) {
        Map<String, String> error = new HashMap<>();
        error.put("message", "Failed to fetch messages: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

---

### 3. Send a Message

**Endpoint:** `POST /messages`  
**Authorization:** `@PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")`  
**Request Body:** `CreateMessageRequest`  
**Response:** `MessageDTO`

**Implementation:**
```java
@PostMapping
@PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
public ResponseEntity<?> sendMessage(
        @RequestBody CreateMessageRequest request,
        Authentication authentication) {
    try {
        String username = authentication.getName();
        User sender = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify user is participant
        if (!messageService.isUserParticipant(request.getConversationId(), sender.getId())) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Unauthorized: You are not a participant in this conversation");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        MessageDTO message = messageService.sendMessage(
                request.getConversationId(),
                sender.getId(),
                request.getContent()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    } catch (Exception e) {
        Map<String, String> error = new HashMap<>();
        error.put("message", "Failed to send message: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

---

### 4. Create Direct Conversation (1-on-1)

**Endpoint:** `POST /messages/conversations/direct`  
**Authorization:** `@PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")`  
**Request Body:** `{ "participantId": 123 }`  
**Response:** `ConversationDTO`

**Implementation:**
```java
@PostMapping("/conversations/direct")
@PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
public ResponseEntity<?> createDirectConversation(
        @RequestBody Map<String, Long> request,
        Authentication authentication) {
    try {
        String username = authentication.getName();
        User currentUser = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long participantId = request.get("participantId");
        if (participantId == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "participantId is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        // Check if conversation already exists
        ConversationDTO existing = messageService.findDirectConversation(
                currentUser.getId(), participantId);
        if (existing != null) {
            return ResponseEntity.ok(existing);
        }

        // Create new direct conversation
        ConversationDTO conversation = messageService.createDirectConversation(
                currentUser.getId(), participantId);

        return ResponseEntity.status(HttpStatus.CREATED).body(conversation);
    } catch (Exception e) {
        Map<String, String> error = new HashMap<>();
        error.put("message", "Failed to create conversation: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

---

### 5. Get Class Group Conversation

**Endpoint:** `GET /messages/conversations/class/{classId}`  
**Authorization:** `@PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")`  
**Response:** `ConversationDTO`

**Implementation:**
```java
@GetMapping("/conversations/class/{classId}")
@PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
public ResponseEntity<?> getClassConversation(
        @PathVariable Long classId,
        Authentication authentication) {
    try {
        String username = authentication.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify user has access to this class
        // (Teacher owns class, or Student is enrolled)
        if (!classService.hasAccessToClass(classId, user.getId())) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Unauthorized: You don't have access to this class");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        ConversationDTO conversation = messageService.getClassConversation(classId);
        
        // If conversation doesn't exist, create it
        if (conversation == null) {
            conversation = messageService.createClassConversation(classId);
        }

        return ResponseEntity.ok(conversation);
    } catch (Exception e) {
        Map<String, String> error = new HashMap<>();
        error.put("message", "Failed to get class conversation: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

---

### 6. Get Students in Class (for Teacher to start direct chats)

**Endpoint:** `GET /classes/{classId}/students`  
**Authorization:** `@PreAuthorize("hasRole('TEACHER')")`  
**Response:** `List<UserDTO>` or `List<ParticipantDTO>`

**Implementation:**
```java
@GetMapping("/{classId}/students")
@PreAuthorize("hasRole('TEACHER')")
public ResponseEntity<?> getClassStudents(
        @PathVariable Long classId,
        Authentication authentication) {
    try {
        String username = authentication.getName();
        User teacher = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        // Verify teacher owns this class
        ClassEntity classEntity = classService.getClassById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        if (!classEntity.getTeacher().getId().equals(teacher.getId())) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Unauthorized: You don't own this class");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        List<ParticipantDTO> students = classService.getClassStudents(classId);
        return ResponseEntity.ok(students);
    } catch (Exception e) {
        Map<String, String> error = new HashMap<>();
        error.put("message", "Failed to fetch students: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

---

### 7. Get All Teachers (for Teacher-to-Teacher chats)

**Endpoint:** `GET /teachers`  
**Authorization:** `@PreAuthorize("hasRole('TEACHER')")`  
**Response:** `List<ParticipantDTO>`

**Implementation:**
```java
@GetMapping("/teachers")
@PreAuthorize("hasRole('TEACHER')")
public ResponseEntity<?> getAllTeachers(Authentication authentication) {
    try {
        String username = authentication.getName();
        User currentTeacher = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        List<ParticipantDTO> teachers = userService.getAllTeachers();
        
        // Remove current teacher from list
        teachers = teachers.stream()
                .filter(t -> !t.getId().equals(currentTeacher.getId()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(teachers);
    } catch (Exception e) {
        Map<String, String> error = new HashMap<>();
        error.put("message", "Failed to fetch teachers: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

---

## Service Layer Methods

### MessageService Interface

```java
package com.elearnhub.teacher_service.service;

import com.elearnhub.teacher_service.dto.*;
import java.util.List;

public interface MessageService {
    // Get all conversations for a user
    List<ConversationDTO> getConversationsByUser(Long userId);

    // Get messages for a conversation
    List<MessageDTO> getMessagesByConversation(Long conversationId, int page, int size);

    // Send a message
    MessageDTO sendMessage(Long conversationId, Long senderId, String content);

    // Create direct conversation (1-on-1)
    ConversationDTO createDirectConversation(Long userId1, Long userId2);

    // Find existing direct conversation
    ConversationDTO findDirectConversation(Long userId1, Long userId2);

    // Create class group conversation
    ConversationDTO createClassConversation(Long classId);

    // Get class conversation
    ConversationDTO getClassConversation(Long classId);

    // Check if user is participant
    boolean isUserParticipant(Long conversationId, Long userId);

    // Mark messages as read
    void markMessagesAsRead(Long conversationId, Long userId);

    // Add participant to conversation (for when student enrolls)
    void addParticipantToConversation(Long conversationId, Long userId);

    // Remove participant from conversation
    void removeParticipantFromConversation(Long conversationId, Long userId);
}
```

---

## Auto-Creation Logic

### When Class is Created

**In ClassService.createClass():**
```java
public ClassDTO createClass(Long teacherId, Long courseId, String name) {
    // ... existing class creation code ...
    
    ClassEntity savedClass = classEntityRepository.save(classEntity);
    
    // ✅ AUTO-CREATE: Create group conversation for this class
    try {
        messageService.createClassConversation(savedClass.getId());
    } catch (Exception e) {
        // Log error but don't fail class creation
        System.err.println("Failed to create class conversation: " + e.getMessage());
    }
    
    return new ClassDTO(savedClass.getId(), savedClass.getName(), teacherId, courseId);
}
```

### When Student is Added to Class

**In ClassService.addStudentToClass():**
```java
public void addStudentToClass(Long classId, Long studentId) {
    // ... existing code to add student ...
    
    // ✅ AUTO-ADD: Add student to class group conversation
    try {
        ConversationDTO classConversation = messageService.getClassConversation(classId);
        if (classConversation != null) {
            messageService.addParticipantToConversation(classConversation.getId(), studentId);
        }
    } catch (Exception e) {
        // Log error but don't fail student addition
        System.err.println("Failed to add student to class conversation: " + e.getMessage());
    }
}
```

---

## Controller Structure

### MessageController.java

```java
package com.elearnhub.teacher_service.Controller;

import com.elearnhub.teacher_service.dto.*;
import com.elearnhub.teacher_service.service.MessageService;
import com.elearnhub.teacher_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    // All endpoints listed above go here...
}
```

---

## Summary of Required Endpoints

1. ✅ `GET /messages/conversations` - Get all conversations
2. ✅ `GET /messages/conversations/{conversationId}/messages` - Get messages
3. ✅ `POST /messages` - Send message
4. ✅ `POST /messages/conversations/direct` - Create direct chat
5. ✅ `GET /messages/conversations/class/{classId}` - Get class group chat
6. ✅ `GET /classes/{classId}/students` - Get students in class (for teacher)
7. ✅ `GET /teachers` - Get all teachers (for teacher-to-teacher chats)

---

## Database Tables

```sql
-- Conversations table
CREATE TABLE conversations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    type VARCHAR(20) NOT NULL, -- 'DIRECT' or 'GROUP'
    name VARCHAR(255),
    class_id BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    FOREIGN KEY (class_id) REFERENCES classes(id)
);

-- Messages table
CREATE TABLE messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content VARCHAR(2000) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    read_at TIMESTAMP,
    FOREIGN KEY (conversation_id) REFERENCES conversations(id),
    FOREIGN KEY (sender_id) REFERENCES users(id)
);

-- Conversation participants (many-to-many)
CREATE TABLE conversation_participants (
    conversation_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    last_read_at TIMESTAMP,
    unread_count INT DEFAULT 0,
    PRIMARY KEY (conversation_id, user_id),
    FOREIGN KEY (conversation_id) REFERENCES conversations(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Indexes for performance
CREATE INDEX idx_messages_conversation ON messages(conversation_id, created_at DESC);
CREATE INDEX idx_conversations_class ON conversations(class_id);
CREATE INDEX idx_participants_user ON conversation_participants(user_id);
```

---

## Implementation Priority

### Phase 1 (Core Functionality):
1. ✅ Create entities and DTOs
2. ✅ Create MessageService
3. ✅ Implement basic endpoints (get conversations, get messages, send message)
4. ✅ Auto-create class conversations

### Phase 2 (Enhanced Features):
5. ✅ Direct conversation creation
6. ✅ Auto-add students to class groups
7. ✅ Read receipts and unread counts
8. ✅ Get students/teachers lists

### Phase 3 (Nice to Have):
9. File attachments
10. Message search
11. Typing indicators
12. Online status

---

## Notes

- **Auto-creation**: Class conversations are created automatically when a class is created
- **Auto-enrollment**: Students are automatically added to class conversations when enrolled
- **Direct chats**: Created on-demand when user starts chatting with someone
- **Security**: All endpoints verify user has access to the conversation/class
- **Pagination**: Message endpoints support pagination for large conversations

