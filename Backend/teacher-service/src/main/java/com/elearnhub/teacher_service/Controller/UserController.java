package com.elearnhub.teacher_service.Controller;

import com.elearnhub.teacher_service.dto.ChangePasswordRequest;
import com.elearnhub.teacher_service.dto.ParticipantDTO;
import com.elearnhub.teacher_service.dto.ProfileDTO;
import com.elearnhub.teacher_service.entity.User;
import com.elearnhub.teacher_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Value("${file.upload-dir:uploads/profiles}")
    private String uploadDir;
    
    // Change password
    @PutMapping("/change-password")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<?> changePassword(
            @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Validate current password
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Current password is incorrect");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            // Validate new password
            if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "New password must be at least 6 characters long");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            // Update password
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userService.save(user);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Password changed successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to change password: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // Get current user profile
    @GetMapping("/profile")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT') or hasRole('ADMIN')")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            ProfileDTO profile = convertToProfileDTO(user);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to get profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    

    
    // Update profile information
    @PutMapping("/profile")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT') or hasRole('ADMIN')")
    public ResponseEntity<?> updateProfile(
            @RequestBody Map<String, String> updates,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Validate and update allowed fields
            if (updates.containsKey("name")) {
                String name = updates.get("name");
                if (name != null && !name.trim().isEmpty()) {
                    user.setName(name.trim());
                }
            }
            if (updates.containsKey("email")) {
                String email = updates.get("email");
                if (email != null && email.contains("@")) {
                    user.setEmail(email.trim());
                } else if (email != null) {
                    Map<String, String> error = new HashMap<>();
                    error.put("message", "Invalid email format");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
                }
            }
            if (updates.containsKey("phoneNumber")) {
                user.setPhoneNumber(updates.get("phoneNumber"));
            }
            if (updates.containsKey("address")) {
                user.setAddress(updates.get("address"));
            }
            
            userService.save(user);
            
            ProfileDTO profile = convertToProfileDTO(user);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Profile updated successfully");
            response.put("profile", profile);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to update profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // Upload profile picture
    @PostMapping(value = "/profile/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT') or hasRole('ADMIN')")
    public ResponseEntity<?> uploadProfilePicture(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Validate file
            if (file.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Please select a file to upload");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Only image files are allowed");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            // Create upload directory if it doesn't exist
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
            String uniqueFilename = "profile_" + user.getId() + "_" + UUID.randomUUID().toString() + fileExtension;
            
            // Delete old profile picture if exists
            if (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
                try {
                    Path oldFilePath = Paths.get(uploadDir, user.getProfilePicture());
                    Files.deleteIfExists(oldFilePath);
                } catch (IOException e) {
                    // Log but don't fail if old file can't be deleted
                    System.err.println("Could not delete old profile picture: " + e.getMessage());
                }
            }
            
            // Save file to disk
            Path filePath = Paths.get(uploadDir, uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Update user profile picture path
            user.setProfilePicture(uniqueFilename);
            userService.save(user);
            
            ProfileDTO profile = convertToProfileDTO(user);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Profile picture uploaded successfully");
            response.put("profile", profile);
            response.put("profilePicture", uniqueFilename);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to upload profile picture: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // Delete profile picture
    @DeleteMapping("/profile/picture")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteProfilePicture(Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Delete file if exists
            if (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
                try {
                    Path filePath = Paths.get(uploadDir, user.getProfilePicture());
                    Files.deleteIfExists(filePath);
                } catch (IOException e) {
                    System.err.println("Could not delete profile picture file: " + e.getMessage());
                }
            }
            
            // Remove from user
            user.setProfilePicture(null);
            userService.save(user);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Profile picture deleted successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to delete profile picture: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // ✅ ADD: Get all teachers (for messaging)
    @GetMapping("/teachers")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<?> getAllTeachers() {
        try {
            List<ParticipantDTO> teachers = userService.getAllTeachers();
            return ResponseEntity.ok(teachers);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to fetch teachers: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // ✅ ADD: Get all students (for messaging and class management)
    @GetMapping("/students")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<?> getAllStudents() {
        try {
            List<ParticipantDTO> students = userService.getAllStudents();
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to fetch students: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Helper method to convert User to ProfileDTO
    private ProfileDTO convertToProfileDTO(User user) {
        return new ProfileDTO(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getAddress(),
                user.getProfilePicture(),
                user.getRole()
        );
    }
    
    // Helper method to convert User to ParticipantDTO
    private ParticipantDTO convertToParticipantDTO(User user) {
        ParticipantDTO dto = new ParticipantDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setName(user.getName());
        dto.setRole(user.getRole());
        dto.setAvatar(user.getProfilePicture());
        return dto;
    }
}
