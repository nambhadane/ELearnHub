# Messaging Endpoints Fix - Missing /teachers and Null Response Issues

## Issues Found

1. **`GET /teachers` returns 404** - Endpoint doesn't exist
2. **`GET /messages/conversations/class/7` returns `application/octet-stream`** - Response is null because MessageService is not implemented

## Fixes

---

## 1. Add `/teachers` Endpoint to MessageController

**Add this endpoint to your `MessageController`:**

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

**OR add it to `TeacherController` (if you prefer):**

```java
// In TeacherController.java
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

**Note:** If you add it to `TeacherController`, the URL will be `/teacher/teachers`. If you add it to `MessageController`, the URL will be `/messages/teachers`. 

**Recommendation:** Add it to `MessageController` at `/messages/teachers` OR create a separate endpoint at root level `/teachers` in a new controller or existing one.

---

## 2. Fix `getClassConversation` to Handle Null MessageService

**Update the `getClassConversation` method in `MessageController`:**

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
        if (!classService.hasAccessToClass(classId, user.getId())) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Unauthorized: You don't have access to this class");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        // ✅ FIX: Check if MessageService is available
        if (messageService == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "MessageService is not yet implemented. Please implement MessageService first.");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
        }

        ConversationDTO conversation = messageService.getClassConversation(classId);
        
        // If conversation doesn't exist, create it
        if (conversation == null) {
            conversation = messageService.createClassConversation(classId);
        }

        // ✅ FIX: Return proper JSON response even if conversation is null
        if (conversation == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to create or retrieve class conversation");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
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

## 3. Complete MessageController with All Endpoints

**Here's the complete `MessageController` with all endpoints and proper null handling:**

```java
package com.elearnhub.teacher_service.Controller;

import com.elearnhub.teacher_service.dto.*;
import com.elearnhub.teacher_service.entity.User;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/messages")
public class MessageController {

    @Autowired(required = false)  // ✅ Make optional
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private ClassService classService;

    // Helper method to check MessageService
    private void checkMessageService() {
        if (messageService == null) {
            throw new RuntimeException("MessageService is not yet implemented. Please implement MessageService first.");
        }
    }

    @GetMapping("/conversations")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<?> getConversations(Authentication authentication) {
        checkMessageService();
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
        checkMessageService();
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
        checkMessageService();
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
        checkMessageService();
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
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Verify user has access to this class
            if (!classService.hasAccessToClass(classId, user.getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Unauthorized: You don't have access to this class");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            // ✅ FIX: Check if MessageService is available
            if (messageService == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "MessageService is not yet implemented. Please implement MessageService first.");
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
            }

            ConversationDTO conversation = messageService.getClassConversation(classId);
            
            // If conversation doesn't exist, create it
            if (conversation == null) {
                conversation = messageService.createClassConversation(classId);
            }

            // ✅ FIX: Return proper JSON response even if conversation is null
            if (conversation == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Failed to create or retrieve class conversation");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
            }

            return ResponseEntity.ok(conversation);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to get class conversation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ ADD THIS ENDPOINT - Get all teachers
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
}
```

**Add these imports:**
```java
import java.util.stream.Collectors;
```

---

## 4. Alternative: Add `/teachers` to Root Level (Separate Controller)

If you want `/teachers` at root level (not `/messages/teachers`), create a new endpoint in `TeacherController` or create a new controller:

**Option A: Add to TeacherController**
```java
// In TeacherController.java
@GetMapping("/teachers")  // This will be /teacher/teachers
@PreAuthorize("hasRole('TEACHER')")
public ResponseEntity<?> getAllTeachers(Authentication authentication) {
    // ... same implementation as above
}
```

**Option B: Create Root-Level Endpoint**
```java
// In a new controller or existing one
@RestController
@RequestMapping("/api")  // or just "/"
public class TeacherListController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/teachers")  // This will be /api/teachers or /teachers
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> getAllTeachers(Authentication authentication) {
        // ... same implementation
    }
}
```

**Option C: Add to MessageController (Recommended)**
- URL: `/messages/teachers`
- Keeps all messaging-related endpoints together

---

## 5. Update Frontend API if Needed

If you add `/teachers` to `MessageController`, the URL will be `/messages/teachers`. Update your frontend API:

**Current (in api.ts):**
```typescript
export async function getAllTeachers(): Promise<ParticipantDTO[]> {
  const response = await fetch(`${API_BASE_URL}/teachers`, {
```

**If you add to MessageController, change to:**
```typescript
export async function getAllTeachers(): Promise<ParticipantDTO[]> {
  const response = await fetch(`${API_BASE_URL}/messages/teachers`, {
```

**OR if you add to TeacherController:**
```typescript
export async function getAllTeachers(): Promise<ParticipantDTO[]> {
  const response = await fetch(`${API_BASE_URL}/teacher/teachers`, {
```

---

## Summary

1. ✅ **Add `/teachers` endpoint** to `MessageController` (or `TeacherController`)
2. ✅ **Fix `getClassConversation`** to handle null MessageService properly
3. ✅ **Return proper JSON error responses** instead of null
4. ✅ **Update frontend API URL** if endpoint location changes

After these fixes:
- `/teachers` endpoint will work (404 fixed)
- `/messages/conversations/class/{classId}` will return proper JSON error instead of octet-stream
- All endpoints will handle missing MessageService gracefully

