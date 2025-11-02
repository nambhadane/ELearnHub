package com.elearnhub.teacher_service.Controller;

import com.elearnhub.teacher_service.entity.User;
import com.elearnhub.teacher_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/teacher")
public class TeacherController {

    @Autowired
    private UserService userService;

    // Profile picture upload directory
    @Value("${file.profile-upload-dir:uploads/profiles}")
    private String profileUploadDir;

    // ✅ Get teacher profile
    @GetMapping("/profile")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        try {
            // Get username from authentication
            String username = authentication.getName();
            
            // Fetch user details from database
            Optional<User> userOptional = userService.findByUsername(username);
            
            if (userOptional.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            User user = userOptional.get();
            
            // Return user profile (exclude password)
            Map<String, Object> profile = new HashMap<>();
            profile.put("id", user.getId());
            profile.put("username", user.getUsername());
            profile.put("name", user.getUsername() != null ? user.getUsername() : "");
            profile.put("email", user.getEmail() != null ? user.getEmail() : "");
            profile.put("profilePicture", user.getProfilePicture() != null ? user.getProfilePicture() : "");
            
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to fetch profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ Update teacher profile
    @PutMapping("/profile")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> updateProfile(
            @RequestBody Map<String, String> profileUpdate,
            Authentication authentication) {
        try {
            // Get username from authentication
            String username = authentication.getName();
            
            // Fetch current user
            Optional<User> userOptional = userService.findByUsername(username);
            
            if (userOptional.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            User user = userOptional.get();
            
            // Preserve original username and role - NEVER change these
            String originalUsername = user.getUsername();
            String originalRole = user.getRole();
            
            // Update only name and email (username and role cannot be changed)
            if (profileUpdate.containsKey("name")) {
                user.setUsername(profileUpdate.get("name"));
            }
            
            if (profileUpdate.containsKey("email")) {
                String email = profileUpdate.get("email");
                if (email != null && !email.trim().isEmpty()) {
                    user.setEmail(email.trim());
                }
            }
            
            // Explicitly preserve username and role to prevent accidental changes
            user.setUsername(originalUsername);
            user.setRole(originalRole);
            
            // Save updated user
            User updatedUser = userService.updateUser(user.getId(), user);
            
            // Return updated profile
            Map<String, Object> profile = new HashMap<>();
            profile.put("id", updatedUser.getId());
            profile.put("username", updatedUser.getUsername());
            profile.put("name", updatedUser.getUsername() != null ? updatedUser.getUsername() : "");
            profile.put("email", updatedUser.getEmail() != null ? updatedUser.getEmail() : "");
            profile.put("profilePicture", updatedUser.getProfilePicture() != null ? updatedUser.getProfilePicture() : "");
            
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to update profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ Upload profile picture
    @PostMapping("/profile/picture")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> uploadProfilePicture(
            @RequestPart MultipartFile file,
            Authentication authentication) {
        try {
            // Validate file
            if (file.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "File is empty");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            // Validate file type (images only)
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "File must be an image");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            // Validate file size (max 5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "File size must be less than 5MB");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            // Get authenticated teacher
            String username = authentication.getName();
            Optional<User> userOptional = userService.findByUsername(username);
            
            if (userOptional.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            User user = userOptional.get();

            // Save file
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path uploadPath = Paths.get(profileUploadDir);
            
            // Create directory if it doesn't exist
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Update user's profile picture path
            String profilePicturePath = "/profiles/" + fileName;
            user.setProfilePicture(profilePicturePath);
            User updatedUser = userService.updateUser(user.getId(), user);

            // Return updated profile
            Map<String, Object> profile = new HashMap<>();
            profile.put("id", updatedUser.getId());
            profile.put("username", updatedUser.getUsername());
            profile.put("name", updatedUser.getUsername() != null ? updatedUser.getUsername() : "");
            profile.put("email", updatedUser.getEmail() != null ? updatedUser.getEmail() : "");
            profile.put("profilePicture", updatedUser.getProfilePicture());
            
            return ResponseEntity.ok(profile);
        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to save file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to upload profile picture: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ Get profile picture (serve file)
    @GetMapping("/profile/picture")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> getProfilePicture(Authentication authentication) {
        try {
            String username = authentication.getName();
            Optional<User> userOptional = userService.findByUsername(username);
            
            if (userOptional.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            User user = userOptional.get();
            if (user.getProfilePicture() == null || user.getProfilePicture().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Profile picture not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            // Remove leading slash if present and construct file path
            String picturePath = user.getProfilePicture().startsWith("/") 
                ? user.getProfilePicture().substring(1) 
                : user.getProfilePicture();
            
            Path filePath = Paths.get(profileUploadDir).resolve(picturePath.replace("/profiles/", ""));
            
            if (!Files.exists(filePath)) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Profile picture file not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            byte[] fileBytes = Files.readAllBytes(filePath);
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "image/jpeg"; // Default
            }

            return ResponseEntity.ok()
                    .header("Content-Type", contentType)
                    .body(fileBytes);
        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to read profile picture: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to get profile picture: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping("/admin/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String newPassword = request.get("password");
            
            if (username == null || newPassword == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Username and password are required");
                return ResponseEntity.badRequest().body(error);
            }
            
            boolean success = userService.resetPassword(username, newPassword);
            
            if (success) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Password reset successfully");
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to reset password: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}

