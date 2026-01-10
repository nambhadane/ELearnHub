# MessageService Optional Fix - App Won't Start

## Problem
`MessageService` is not implemented yet, but `MessageController` and `ClassServiceImpl` are trying to autowire it, causing the application to fail to start.

## Solution
Make `MessageService` optional (`required=false`) in all places where it's autowired until you implement it.

---

## 1. Fix MessageController

**Change:**
```java
@Autowired
private MessageService messageService;
```

**To:**
```java
@Autowired(required = false)  // ✅ Make it optional
private MessageService messageService;
```

**And add null checks in methods that use it:**

```java
package com.elearnhub.teacher_service.Controller;

import com.elearnhub.teacher_service.dto.*;
import com.elearnhub.teacher_service.service.MessageService;
import com.elearnhub.teacher_service.service.UserService;
import com.elearnhub.teacher_service.service.ClassService;
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

    @Autowired(required = false)  // ✅ Make optional
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private ClassService classService;

    // Add null check helper
    private void checkMessageService() {
        if (messageService == null) {
            throw new RuntimeException("MessageService is not yet implemented. Please implement MessageService first.");
        }
    }

    @GetMapping("/conversations")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<?> getConversations(Authentication authentication) {
        checkMessageService();  // ✅ Check if service exists
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

    @GetMapping("/conversations/{conversationId}/messages")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<?> getMessages(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            Authentication authentication) {
        checkMessageService();  // ✅ Check if service exists
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!messageService.isUserParticipant(conversationId, user.getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Unauthorized: You are not a participant in this conversation");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            List<MessageDTO> messages = messageService.getMessagesByConversation(conversationId, page, size);
            messageService.markMessagesAsRead(conversationId, user.getId());

            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to fetch messages: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<?> sendMessage(
            @RequestBody CreateMessageRequest request,
            Authentication authentication) {
        checkMessageService();  // ✅ Check if service exists
        try {
            String username = authentication.getName();
            User sender = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

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

    @PostMapping("/conversations/direct")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<?> createDirectConversation(
            @RequestBody Map<String, Long> request,
            Authentication authentication) {
        checkMessageService();  // ✅ Check if service exists
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

            ConversationDTO existing = messageService.findDirectConversation(
                    currentUser.getId(), participantId);
            if (existing != null) {
                return ResponseEntity.ok(existing);
            }

            ConversationDTO conversation = messageService.createDirectConversation(
                    currentUser.getId(), participantId);

            return ResponseEntity.status(HttpStatus.CREATED).body(conversation);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to create conversation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/conversations/class/{classId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<?> getClassConversation(
            @PathVariable Long classId,
            Authentication authentication) {
        checkMessageService();  // ✅ Check if service exists
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!classService.hasAccessToClass(classId, user.getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Unauthorized: You don't have access to this class");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            ConversationDTO conversation = messageService.getClassConversation(classId);
            
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
}
```

---

## 2. Verify ClassServiceImpl Already Has Optional

Your `ClassServiceImpl` should already have `@Autowired(required = false)`:

```java
@Autowired(required = false)  // ✅ Already optional - won't break if not implemented
private MessageService messageService;
```

And it should have null checks:

```java
// ✅ AUTO-CREATE: Create group conversation for this class
try {
    if (messageService != null) {  // ✅ Check if service exists
        messageService.createClassConversation(savedClass.getId());
    }
} catch (Exception e) {
    System.err.println("Failed to create class conversation: " + e.getMessage());
    e.printStackTrace();
}
```

---

## 3. Alternative: Create a Stub MessageService (Temporary)

If you want the app to start without errors, create a simple stub implementation:

```java
package com.elearnhub.teacher_service.service;

import com.elearnhub.teacher_service.dto.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

    // Stub implementation - returns empty lists/null until you implement properly
    
    @Override
    public List<ConversationDTO> getConversationsByUser(Long userId) {
        return new ArrayList<>();  // Return empty list for now
    }

    @Override
    public List<MessageDTO> getMessagesByConversation(Long conversationId, int page, int size) {
        return new ArrayList<>();  // Return empty list for now
    }

    @Override
    public MessageDTO sendMessage(Long conversationId, Long senderId, String content) {
        throw new RuntimeException("MessageService not yet implemented");
    }

    @Override
    public ConversationDTO createDirectConversation(Long userId1, Long userId2) {
        throw new RuntimeException("MessageService not yet implemented");
    }

    @Override
    public ConversationDTO findDirectConversation(Long userId1, Long userId2) {
        return null;
    }

    @Override
    public ConversationDTO createClassConversation(Long classId) {
        // Return null for now - won't break class creation
        return null;
    }

    @Override
    public ConversationDTO getClassConversation(Long classId) {
        return null;
    }

    @Override
    public boolean isUserParticipant(Long conversationId, Long userId) {
        return false;
    }

    @Override
    public void markMessagesAsRead(Long conversationId, Long userId) {
        // Do nothing for now
    }

    @Override
    public void addParticipantToConversation(Long conversationId, Long userId) {
        // Do nothing for now
    }

    @Override
    public void removeParticipantFromConversation(Long conversationId, Long userId) {
        // Do nothing for now
    }
}
```

---

## Quick Fix Summary

**Option 1 (Recommended - Make Optional):**
1. Change `MessageController` to use `@Autowired(required = false)`
2. Add null checks in all methods
3. App will start, but messaging endpoints will return errors until you implement MessageService

**Option 2 (Create Stub):**
1. Create a stub `MessageServiceImpl` that implements all methods (returns empty/null)
2. App will start and work, but messaging features won't function until you implement properly

**Option 3 (Comment Out):**
1. Comment out all `MessageService` dependencies
2. Comment out messaging endpoints in `MessageController`
3. Implement MessageService later

---

## Recommended: Use Option 1

Make `MessageService` optional in `MessageController`:

```java
@Autowired(required = false)
private MessageService messageService;

private void checkMessageService() {
    if (messageService == null) {
        throw new RuntimeException("MessageService is not yet implemented");
    }
}
```

Then call `checkMessageService()` at the start of each endpoint method.

This way:
- ✅ App will start successfully
- ✅ Other features will work
- ✅ Messaging endpoints will return clear error messages
- ✅ You can implement MessageService later without breaking anything

