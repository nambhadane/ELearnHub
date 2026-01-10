// ============================================
// Updated MessageController - Support File Uploads
// ============================================

package com.elearnhub.teacher_service.Controller;

import com.elearnhub.teacher_service.dto.*;
import com.elearnhub.teacher_service.entity.User;
import com.elearnhub.teacher_service.service.MessageService;
import com.elearnhub.teacher_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@RestController
@RequestMapping("/messages")
public class MessageController {

    @Autowired(required = false)
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Value("${file.upload-dir:uploads/messages}")
    private String uploadDir;

    private void checkMessageService() {
        if (messageService == null) {
            throw new RuntimeException("MessageService is not yet implemented. Please implement MessageService first.");
        }
    }

    // ✅ UPDATED: Send message with file support
    @PostMapping(consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<?> sendMessage(
            @RequestParam(required = false) Long conversationId,
            @RequestParam(required = false) String content,
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            Authentication authentication) {
        checkMessageService();
        try {
            String username = authentication.getName();
            User sender = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Validate required parameters
            if (conversationId == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "conversationId is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            String msgContent = content != null ? content : "";
            List<MultipartFile> fileList = new ArrayList<>();
            if (files != null) {
                fileList = Arrays.asList(files);
            }

            if (!messageService.isUserParticipant(conversationId, sender.getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Unauthorized: You are not a participant in this conversation");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            // Save files if provided
            List<String> filePaths = new ArrayList<>();
            if (!fileList.isEmpty()) {
                try {
                    // Create upload directory if it doesn't exist
                    Path uploadPath = Paths.get(uploadDir);
                    if (!Files.exists(uploadPath)) {
                        Files.createDirectories(uploadPath);
                    }

                    // File size limit: 10MB
                    long maxFileSize = 10 * 1024 * 1024; // 10MB in bytes

                    for (MultipartFile file : fileList) {
                        if (!file.isEmpty()) {
                            // Validate file size
                            if (file.getSize() > maxFileSize) {
                                Map<String, String> error = new HashMap<>();
                                error.put("message", "File " + file.getOriginalFilename() + " exceeds 10MB limit");
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
                            }

                            // Generate unique filename
                            String originalFilename = file.getOriginalFilename();
                            String timestamp = String.valueOf(System.currentTimeMillis());
                            String filename = timestamp + "_" + originalFilename;

                            // Save file
                            Path filePath = uploadPath.resolve(filename);
                            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                            filePaths.add(uploadDir + "/" + filename);
                        }
                    }
                } catch (IOException e) {
                    Map<String, String> error = new HashMap<>();
                    error.put("message", "Failed to save files: " + e.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
                }
            }

            // Send message with file paths
            MessageDTO message = messageService.sendMessage(
                    conversationId,
                    sender.getId(),
                    msgContent,
                    filePaths
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(message);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to send message: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ ADD: Download message file
    @GetMapping("/files/{filename}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<?> downloadFile(
            @PathVariable String filename,
            Authentication authentication) {
        try {
            // Get absolute path for upload directory
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path filePath = uploadPath.resolve(filename).normalize();
            
            // Security check - ensure file is within upload directory
            if (!filePath.startsWith(uploadPath)) {
                System.err.println("Security violation: File path " + filePath + " is outside upload directory " + uploadPath);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            File file = filePath.toFile();
            if (!file.exists()) {
                System.err.println("File not found: " + filePath);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            org.springframework.core.io.Resource resource = 
                new org.springframework.core.io.UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                System.err.println("File not readable: " + filePath);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + file.getName() + "\"")
                    .contentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            System.err.println("Error downloading file: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ ADD: Get conversations for authenticated user
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

    // ✅ ADD: Get messages in a conversation
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

            // Check if user is participant in conversation
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

    // ✅ ADD: Get class conversation
    @GetMapping("/conversations/class/{classId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<?> getClassConversation(
            @PathVariable Long classId,
            Authentication authentication) {
        checkMessageService();
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // TODO: Add class access validation here if needed
            
            ConversationDTO conversation = messageService.getClassConversation(classId);
            
            if (conversation == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Class conversation not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            return ResponseEntity.ok(conversation);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to get class conversation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ ADD: Create direct conversation
    @PostMapping("/conversations/direct")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<?> createDirectConversation(
            @RequestParam Long userId,
            Authentication authentication) {
        checkMessageService();
        try {
            String username = authentication.getName();
            User currentUser = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Check if conversation already exists
            ConversationDTO existingConversation = messageService.findDirectConversation(currentUser.getId(), userId);
            if (existingConversation != null) {
                return ResponseEntity.ok(existingConversation);
            }

            // Create new conversation
            ConversationDTO conversation = messageService.createDirectConversation(currentUser.getId(), userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(conversation);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to create conversation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ DEBUG: Check if MessageService is working
    @GetMapping("/debug/status")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<?> debugStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("messageServiceAvailable", messageService != null);
        if (messageService != null) {
            status.put("messageServiceClass", messageService.getClass().getSimpleName());
        }
        return ResponseEntity.ok(status);
    }
}

