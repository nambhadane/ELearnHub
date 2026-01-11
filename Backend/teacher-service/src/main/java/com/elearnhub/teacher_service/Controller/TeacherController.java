//package com.elearnhub.teacher_service.Controller;
//
//import com.elearnhub.teacher_service.entity.User;
//import com.elearnhub.teacher_service.service.UserService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.Authentication;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.nio.file.StandardCopyOption;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Optional;
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/teacher")
//public class TeacherController {
//
//    @Autowired
//    private UserService userService;
//
//    @Value("${file.profile-upload-dir:uploads/profiles}")
//    private String profileUploadDir;
//
//    @GetMapping("/profile")
//    @PreAuthorize("hasRole('TEACHER')")
//    public ResponseEntity<?> getProfile(Authentication authentication) {
//        try {
//            String username = authentication.getName();
//            Optional<User> userOptional = userService.findByUsername(username);
//
//            if (userOptional.isEmpty()) {
//                Map<String, String> error = new HashMap<>();
//                error.put("message", "User not found");
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
//            }
//
//            User user = userOptional.get();
//
//            Map<String, Object> profile = new HashMap<>();
//            profile.put("id", user.getId());
//            profile.put("username", user.getUsername());
//            profile.put("name", user.getName() != null ? user.getName() : "");
//            profile.put("email", user.getEmail() != null ? user.getEmail() : "");
//            profile.put("profilePicture", user.getProfilePicture() != null ? user.getProfilePicture() : "");
//            profile.put("phoneNumber", user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
//            profile.put("address", user.getAddress() != null ? user.getAddress() : "");
//
//            return ResponseEntity.ok(profile);
//        } catch (Exception e) {
//            Map<String, String> error = new HashMap<>();
//            error.put("message", "Failed to fetch profile: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
//        }
//    }
//
//    @PutMapping("/profile")
//    @PreAuthorize("hasRole('TEACHER')")
//    public ResponseEntity<?> updateProfile(
//            @RequestBody Map<String, String> profileUpdate,
//            Authentication authentication) {
//        try {
//            String username = authentication.getName();
//            Optional<User> userOptional = userService.findByUsername(username);
//
//            if (userOptional.isEmpty()) {
//                Map<String, String> error = new HashMap<>();
//                error.put("message", "User not found");
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
//            }
//
//            User user = userOptional.get();
//
//            String originalUsername = user.getUsername();
//            String originalRole = user.getRole();
//
//            if (profileUpdate.containsKey("name")) {
//                String name = profileUpdate.get("name");
//                if (name != null && !name.trim().isEmpty()) {
//                    user.setName(name.trim());
//                }
//            }
//
//            if (profileUpdate.containsKey("email")) {
//                String email = profileUpdate.get("email");
//                if (email != null && !email.trim().isEmpty()) {
//                    user.setEmail(email.trim());
//                }
//            }
//
//            if (profileUpdate.containsKey("phoneNumber")) {
//                String phoneNumber = profileUpdate.get("phoneNumber");
//                if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
//                    user.setPhoneNumber(phoneNumber.trim());
//                } else {
//                    user.setPhoneNumber(null);
//                }
//            }
//
//            if (profileUpdate.containsKey("address")) {
//                String address = profileUpdate.get("address");
//                if (address != null && !address.trim().isEmpty()) {
//                    user.setAddress(address.trim());
//                } else {
//                    user.setAddress(null);
//                }
//            }
//
//            user.setUsername(originalUsername);
//            user.setRole(originalRole);
//
//            User updatedUser = userService.updateUser(user.getId(), user);
//
//            Map<String, Object> profile = new HashMap<>();
//            profile.put("id", updatedUser.getId());
//            profile.put("username", updatedUser.getUsername());
//            profile.put("name", updatedUser.getName() != null ? updatedUser.getName() : "");
//            profile.put("email", updatedUser.getEmail() != null ? updatedUser.getEmail() : "");
//            profile.put("profilePicture", updatedUser.getProfilePicture() != null ? updatedUser.getProfilePicture() : "");
//            profile.put("phoneNumber", updatedUser.getPhoneNumber() != null ? updatedUser.getPhoneNumber() : "");
//            profile.put("address", updatedUser.getAddress() != null ? updatedUser.getAddress() : "");
//
//            return ResponseEntity.ok(profile);
//        } catch (Exception e) {
//            Map<String, String> error = new HashMap<>();
//            error.put("message", "Failed to update profile: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
//        }
//    }
//
//    @PostMapping("/profile/picture")
//    @PreAuthorize("hasRole('TEACHER')")
//    public ResponseEntity<?> uploadProfilePicture(
//            @RequestPart MultipartFile file,
//            Authentication authentication) {
//        try {
//            if (file.isEmpty()) {
//                Map<String, String> error = new HashMap<>();
//                error.put("message", "File is empty");
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
//            }
//
//            String contentType = file.getContentType();
//            if (contentType == null || !contentType.startsWith("image/")) {
//                Map<String, String> error = new HashMap<>();
//                error.put("message", "File must be an image");
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
//            }
//
//            if (file.getSize() > 5 * 1024 * 1024) {
//                Map<String, String> error = new HashMap<>();
//                error.put("message", "File size must be less than 5MB");
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
//            }
//
//            String username = authentication.getName();
//            Optional<User> userOptional = userService.findByUsername(username);
//
//            if (userOptional.isEmpty()) {
//                Map<String, String> error = new HashMap<>();
//                error.put("message", "User not found");
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
//            }
//
//            User user = userOptional.get();
//
//            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
//            Path uploadPath = Paths.get(profileUploadDir);
//
//            if (!Files.exists(uploadPath)) {
//                Files.createDirectories(uploadPath);
//            }
//
//            Path filePath = uploadPath.resolve(fileName);
//            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
//
//            String profilePicturePath = "/profiles/" + fileName;
//            user.setProfilePicture(profilePicturePath);
//
//            User updatedUser = userService.updateUser(user.getId(), user);
//
//            Map<String, Object> profile = new HashMap<>();
//            profile.put("id", updatedUser.getId());
//            profile.put("username", updatedUser.getUsername());
//            profile.put("name", updatedUser.getName() != null ? updatedUser.getName() : "");
//            profile.put("email", updatedUser.getEmail() != null ? updatedUser.getEmail() : "");
//            profile.put("profilePicture", updatedUser.getProfilePicture());
//            profile.put("phoneNumber", updatedUser.getPhoneNumber() != null ? updatedUser.getPhoneNumber() : "");
//            profile.put("address", updatedUser.getAddress() != null ? updatedUser.getAddress() : "");
//
//            return ResponseEntity.ok(profile);
//        } catch (IOException e) {
//            Map<String, String> error = new HashMap<>();
//            error.put("message", "Failed to save file: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
//        } catch (Exception e) {
//            Map<String, String> error = new HashMap<>();
//            error.put("message", "Failed to upload profile picture: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
//        }
//    }
//
//    @GetMapping("/profile/picture")
//    @PreAuthorize("hasRole('TEACHER')")
//    public ResponseEntity<?> getProfilePicture(Authentication authentication) {
//        try {
//            String username = authentication.getName();
//            Optional<User> userOptional = userService.findByUsername(username);
//
//            if (userOptional.isEmpty()) {
//                Map<String, String> error = new HashMap<>();
//                error.put("message", "User not found");
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
//            }
//
//            User user = userOptional.get();
//
//            if (user.getProfilePicture() == null || user.getProfilePicture().isEmpty()) {
//                Map<String, String> error = new HashMap<>();
//                error.put("message", "Profile picture not found");
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
//            }
//
//            String picturePath = user.getProfilePicture().startsWith("/")
//                    ? user.getProfilePicture().substring(1)
//                    : user.getProfilePicture();
//
//            Path filePath = Paths.get(profileUploadDir).resolve(picturePath.replace("/profiles/", ""));
//
//            if (!Files.exists(filePath)) {
//                Map<String, String> error = new HashMap<>();
//                error.put("message", "Profile picture file not found");
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
//            }
//
//            byte[] fileBytes = Files.readAllBytes(filePath);
//            String contentType = Files.probeContentType(filePath);
//            if (contentType == null) {
//                contentType = "image/jpeg";
//            }
//
//            return ResponseEntity.ok()
//                    .header("Content-Type", contentType)
//                    .body(fileBytes);
//        } catch (IOException e) {
//            Map<String, String> error = new HashMap<>();
//            error.put("message", "Failed to read profile picture: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
//        } catch (Exception e) {
//            Map<String, String> error = new HashMap<>();
//            error.put("message", "Failed to get profile picture: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
//        }
//    }
//
//    @PostMapping("/admin/reset-password")
//    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
//        try {
//            String username = request.get("username");
//            String newPassword = request.get("password");
//
//            if (username == null || newPassword == null) {
//                Map<String, String> error = new HashMap<>();
//                error.put("message", "Username and password are required");
//                return ResponseEntity.badRequest().body(error);
//            }
//
//            boolean success = userService.resetPassword(username, newPassword);
//
//            if (success) {
//                Map<String, String> response = new HashMap<>();
//                response.put("message", "Password reset successfully");
//                return ResponseEntity.ok(response);
//            } else {
//                Map<String, String> error = new HashMap<>();
//                error.put("message", "User not found");
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
//            }
//        } catch (Exception e) {
//            Map<String, String> error = new HashMap<>();
//            error.put("message", "Failed to reset password: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
//        }
//    }
//}


package com.elearnhub.teacher_service.Controller;

import com.elearnhub.teacher_service.dto.ParticipantDTO;
import com.elearnhub.teacher_service.entity.User;
import com.elearnhub.teacher_service.service.UserService;
import com.elearnhub.teacher_service.service.ClassService;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/teacher")
public class TeacherController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ClassService classService;
    
    @Value("${file.profile-upload-dir:uploads/profiles}")
    private String profileUploadDir;
    
    // Get teacher profile with student count
    @GetMapping("/profile")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        try {
            String username = authentication.getName();
            System.out.println("🔍 TeacherController: Getting profile for username: " + username);
            
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Teacher not found"));
            
            System.out.println("✅ TeacherController: Found user with ID: " + user.getId());
            
            // Calculate total students across all classes
            int totalStudents = 0;
            try {
                totalStudents = classService.getTotalStudentsByTeacher(user.getId());
                System.out.println("✅ TeacherController: Total students count: " + totalStudents);
            } catch (Exception e) {
                System.err.println("❌ TeacherController: Error calculating student count: " + e.getMessage());
                e.printStackTrace();
                // Continue with 0 students if there's an error
            }
            
            // Create enhanced profile with student count
            Map<String, Object> teacherProfile = new HashMap<>();
            teacherProfile.put("id", user.getId());
            teacherProfile.put("username", user.getUsername());
            teacherProfile.put("name", user.getName());
            teacherProfile.put("email", user.getEmail());
            teacherProfile.put("phoneNumber", user.getPhoneNumber());
            teacherProfile.put("address", user.getAddress());
            teacherProfile.put("profilePicture", user.getProfilePicture());
            teacherProfile.put("role", user.getRole());
            teacherProfile.put("totalStudents", totalStudents);
            
            System.out.println("✅ TeacherController: Returning profile with " + totalStudents + " students");
            return ResponseEntity.ok(teacherProfile);
        } catch (Exception e) {
            System.err.println("❌ TeacherController: Error in getProfile: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to get teacher profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/profile")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> profileUpdate,
                                         Authentication authentication) {
        try {
            String username = authentication.getName();
            Optional<User> userOptional = userService.findByUsername(username);
            
            if (userOptional.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            User user = userOptional.get();
            String originalUsername = user.getUsername();
            String originalRole = user.getRole();
            
            if (profileUpdate.containsKey("name")) {
                String name = profileUpdate.get("name");
                if (name != null && !name.trim().isEmpty()) {
                    user.setName(name.trim());
                }
            }
            
            if (profileUpdate.containsKey("email")) {
                String email = profileUpdate.get("email");
                if (email != null && !email.trim().isEmpty()) {
                    user.setEmail(email.trim());
                }
            }
            
            if (profileUpdate.containsKey("phoneNumber") || profileUpdate.containsKey("phone")) {
                String phoneNumber = profileUpdate.containsKey("phoneNumber") ? 
                    profileUpdate.get("phoneNumber") : profileUpdate.get("phone");
                if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
                    user.setPhoneNumber(phoneNumber.trim());
                } else {
                    user.setPhoneNumber(null);
                }
            }
            
            if (profileUpdate.containsKey("address") || profileUpdate.containsKey("location")) {
                String address = profileUpdate.containsKey("address") ? 
                    profileUpdate.get("address") : profileUpdate.get("location");
                if (address != null && !address.trim().isEmpty()) {
                    user.setAddress(address.trim());
                } else {
                    user.setAddress(null);
                }
            }
            
            user.setUsername(originalUsername);
            user.setRole(originalRole);
            
            User updatedUser = userService.updateUser(user.getId(), user);
            
            // Return updated profile
            Map<String, Object> profile = new HashMap<>();
            profile.put("id", updatedUser.getId());
            profile.put("username", updatedUser.getUsername());
            profile.put("name", updatedUser.getName());
            profile.put("email", updatedUser.getEmail());
            profile.put("phoneNumber", updatedUser.getPhoneNumber());
            profile.put("address", updatedUser.getAddress());
            profile.put("role", updatedUser.getRole());
            
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to update profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping("/profile/picture")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> uploadProfilePicture(@RequestPart MultipartFile file,
                                                Authentication authentication) {
        try {
            if (file.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "File is empty");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "File must be an image");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            if (file.getSize() > 5 * 1024 * 1024) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "File size must be less than 5MB");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            String username = authentication.getName();
            Optional<User> userOptional = userService.findByUsername(username);
            
            if (userOptional.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            User user = userOptional.get();
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path uploadPath = Paths.get(profileUploadDir);
            
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            String profilePicturePath = "/profiles/" + fileName;
            user.setProfilePicture(profilePicturePath);
            User updatedUser = userService.updateUser(user.getId(), user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Profile picture uploaded successfully");
            response.put("profilePicture", profilePicturePath);
            
            return ResponseEntity.ok(response);
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
            
            String picturePath = user.getProfilePicture().startsWith("/") ? 
                user.getProfilePicture().substring(1) : user.getProfilePicture();
            Path filePath = Paths.get(profileUploadDir).resolve(picturePath.replace("/profiles/", ""));
            
            if (!Files.exists(filePath)) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Profile picture file not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            byte[] fileBytes = Files.readAllBytes(filePath);
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "image/jpeg";
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
    
    @GetMapping("/students")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> getAllStudents(Authentication authentication) {
        try {
            String username = authentication.getName();
            User teacher = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
            
            List<ParticipantDTO> students = userService.getAllStudents();
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to fetch students: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}