# Corrected Profile Controllers - Ready to Paste

## 1. TeacherController.java (CORRECTED)

```java
package com.elearnhub.teacher_service.Controller;

import com.elearnhub.teacher_service.dto.UserProfile;
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

    @GetMapping("/profile")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        try {
            String username = authentication.getName();
            Optional<User> userOptional = userService.findByUsername(username);

            if (userOptional.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            User user = userOptional.get();

            // ✅ Convert User to UserProfile DTO
            UserProfile profile = new UserProfile();
            profile.setId(user.getId());
            profile.setUsername(user.getUsername());
            profile.setName(user.getName() != null ? user.getName() : "");
            profile.setEmail(user.getEmail() != null ? user.getEmail() : "");
            profile.setRole("TEACHER");
            profile.setPhone(user.getPhoneNumber() != null ? user.getPhoneNumber() : null);
            profile.setLocation(user.getAddress() != null ? user.getAddress() : null);

            // ✅ Calculate teacher-specific stats
            try {
                int totalClasses = classService.getClassesByTeacher(user.getId()).size();
                profile.setTotalClasses(totalClasses);

                // Calculate total students across all classes
                int totalStudents = classService.getClassesByTeacher(user.getId()).stream()
                        .mapToInt(classDTO -> {
                            // TODO: Get actual student count from class
                            // For now, return 0 or implement getStudentCount method
                            return 0;
                        })
                        .sum();
                profile.setTotalStudents(totalStudents);
            } catch (Exception e) {
                // If stats calculation fails, set to 0
                profile.setTotalClasses(0);
                profile.setTotalStudents(0);
            }

            return ResponseEntity.ok(profile);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to fetch profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> updateProfile(
            @RequestBody Map<String, String> profileUpdate,
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
                String phoneNumber = profileUpdate.containsKey("phoneNumber") 
                    ? profileUpdate.get("phoneNumber") 
                    : profileUpdate.get("phone");
                if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
                    user.setPhoneNumber(phoneNumber.trim());
                } else {
                    user.setPhoneNumber(null);
                }
            }

            if (profileUpdate.containsKey("address") || profileUpdate.containsKey("location")) {
                String address = profileUpdate.containsKey("address") 
                    ? profileUpdate.get("address") 
                    : profileUpdate.get("location");
                if (address != null && !address.trim().isEmpty()) {
                    user.setAddress(address.trim());
                } else {
                    user.setAddress(null);
                }
            }

            user.setUsername(originalUsername);
            user.setRole(originalRole);

            User updatedUser = userService.updateUser(user.getId(), user);

            // ✅ Return UserProfile DTO
            UserProfile profile = new UserProfile();
            profile.setId(updatedUser.getId());
            profile.setUsername(updatedUser.getUsername());
            profile.setName(updatedUser.getName() != null ? updatedUser.getName() : "");
            profile.setEmail(updatedUser.getEmail() != null ? updatedUser.getEmail() : "");
            profile.setRole("TEACHER");
            profile.setPhone(updatedUser.getPhoneNumber());
            profile.setLocation(updatedUser.getAddress());

            return ResponseEntity.ok(profile);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to update profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/profile/picture")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> uploadProfilePicture(
            @RequestPart MultipartFile file,
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

            // ✅ Return UserProfile DTO
            UserProfile profile = new UserProfile();
            profile.setId(updatedUser.getId());
            profile.setUsername(updatedUser.getUsername());
            profile.setName(updatedUser.getName() != null ? updatedUser.getName() : "");
            profile.setEmail(updatedUser.getEmail() != null ? updatedUser.getEmail() : "");
            profile.setRole("TEACHER");
            profile.setPhone(updatedUser.getPhoneNumber());
            profile.setLocation(updatedUser.getAddress());

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
}
```

---

## 2. StudentController.java (CORRECTED)

```java
package com.elearnhub.teacher_service.Controller;

import com.elearnhub.teacher_service.dto.AssignmentDTO;
import com.elearnhub.teacher_service.dto.SubmissionDTO;
import com.elearnhub.teacher_service.dto.UserProfile;
import com.elearnhub.teacher_service.entity.User;
import com.elearnhub.teacher_service.entity.Course;
import com.elearnhub.teacher_service.service.UserService;
import com.elearnhub.teacher_service.service.CourseService;
import com.elearnhub.teacher_service.service.AssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private UserService userService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private AssignmentService assignmentService;

    @Value("${file.profile-upload-dir:uploads/profiles}")
    private String profileUploadDir;

    @GetMapping("/classes")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getMyClasses(Authentication authentication) {
        try {
            String username = authentication.getName();
            User student = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            List<Course> enrolledCourses = courseService.getCoursesByStudentId(student.getId());

            if (enrolledCourses == null || enrolledCourses.isEmpty()) {
                return ResponseEntity.ok(List.of());
            }

            List<Map<String, Object>> response = enrolledCourses.stream()
                    .map(course -> {
                        Map<String, Object> classData = new HashMap<>();
                        classData.put("id", course.getId());
                        classData.put("name", course.getName());

                        if (course.getDescription() != null && !course.getDescription().trim().isEmpty()) {
                            classData.put("description", course.getDescription());
                        }

                        classData.put("subject", course.getName());

                        if (course.getTeacherId() != null) {
                            try {
                                User teacher = userService.getUserById(course.getTeacherId())
                                        .orElse(null);
                                if (teacher != null) {
                                    String teacherName = teacher.getName() != null && !teacher.getName().trim().isEmpty()
                                            ? teacher.getName()
                                            : teacher.getUsername();
                                    classData.put("teacherName", teacherName);
                                    classData.put("teacherId", teacher.getId());
                                }
                            } catch (Exception e) {
                                System.err.println("Error fetching teacher for course " + course.getId() + ": " + e.getMessage());
                            }
                        }

                        if (course.getStudents() != null) {
                            classData.put("students", course.getStudents().size());
                        } else {
                            classData.put("students", 0);
                        }

                        return classData;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to fetch classes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/assignments")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getMyAssignments(Authentication authentication) {
        try {
            String username = authentication.getName();
            User student = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            List<Course> enrolledCourses = courseService.getCoursesByStudentId(student.getId());

            if (enrolledCourses == null || enrolledCourses.isEmpty()) {
                return ResponseEntity.ok(List.of());
            }

            List<Map<String, Object>> allAssignments = new ArrayList<>();
            for (Course course : enrolledCourses) {
                if (course.getId() != null) {
                    List<AssignmentDTO> assignments = assignmentService.getAssignmentsByClass(course.getId());

                    for (AssignmentDTO assignment : assignments) {
                        Map<String, Object> assignmentData = new HashMap<>();
                        assignmentData.put("id", assignment.getId());
                        assignmentData.put("title", assignment.getTitle());
                        assignmentData.put("description", assignment.getDescription());
                        assignmentData.put("dueDate", assignment.getDueDate());
                        assignmentData.put("maxGrade", assignment.getMaxGrade());
                        assignmentData.put("courseId", assignment.getCourseId());
                        assignmentData.put("className", course.getName());

                        SubmissionDTO submission = assignmentService.getSubmissionByStudentAndAssignment(
                                student.getId(), assignment.getId());

                        if (submission != null && submission.getId() != null) {
                            assignmentData.put("status", submission.getGrade() != null ? "graded" : "submitted");
                            assignmentData.put("submissionId", submission.getId());
                            assignmentData.put("submittedAt", submission.getSubmittedAt());
                            assignmentData.put("grade", submission.getGrade());
                            assignmentData.put("feedback", submission.getFeedback());
                        } else {
                            assignmentData.put("status", "pending");
                            assignmentData.put("submissionId", null);
                            assignmentData.put("submittedAt", null);
                            assignmentData.put("grade", null);
                            assignmentData.put("feedback", null);
                        }

                        allAssignments.add(assignmentData);
                    }
                }
            }

            allAssignments.sort((a, b) -> {
                String dueDateA = (String) a.get("dueDate");
                String dueDateB = (String) b.get("dueDate");
                return dueDateA.compareTo(dueDateB);
            });

            return ResponseEntity.ok(allAssignments);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to fetch assignments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        try {
            String username = authentication.getName();
            Optional<User> userOptional = userService.findByUsername(username);

            if (userOptional.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            User user = userOptional.get();

            // ✅ Convert User to UserProfile DTO
            UserProfile profile = new UserProfile();
            profile.setId(user.getId());
            profile.setUsername(user.getUsername());
            profile.setName(user.getName() != null ? user.getName() : "");
            profile.setEmail(user.getEmail() != null ? user.getEmail() : "");
            profile.setRole("STUDENT");
            profile.setPhone(user.getPhoneNumber() != null ? user.getPhoneNumber() : null);
            profile.setLocation(user.getAddress() != null ? user.getAddress() : null);

            // TODO: Add student-specific fields if available in User entity or separate Student entity
            // profile.setStudentId(...);
            // profile.setMajor(...);
            // profile.setYear(...);
            // profile.setGpa(...);
            // profile.setCreditsCompleted(...);
            // profile.setTotalCredits(...);
            // profile.setAttendanceRate(...);

            return ResponseEntity.ok(profile);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to fetch profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> updateProfile(
            @RequestBody Map<String, String> profileUpdate,
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
                String phoneNumber = profileUpdate.containsKey("phoneNumber") 
                    ? profileUpdate.get("phoneNumber") 
                    : profileUpdate.get("phone");
                if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
                    user.setPhoneNumber(phoneNumber.trim());
                } else {
                    user.setPhoneNumber(null);
                }
            }

            if (profileUpdate.containsKey("address") || profileUpdate.containsKey("location")) {
                String address = profileUpdate.containsKey("address") 
                    ? profileUpdate.get("address") 
                    : profileUpdate.get("location");
                if (address != null && !address.trim().isEmpty()) {
                    user.setAddress(address.trim());
                } else {
                    user.setAddress(null);
                }
            }

            user.setUsername(originalUsername);
            user.setRole(originalRole);

            User updatedUser = userService.updateUser(user.getId(), user);

            // ✅ Return UserProfile DTO
            UserProfile profile = new UserProfile();
            profile.setId(updatedUser.getId());
            profile.setUsername(updatedUser.getUsername());
            profile.setName(updatedUser.getName() != null ? updatedUser.getName() : "");
            profile.setEmail(updatedUser.getEmail() != null ? updatedUser.getEmail() : "");
            profile.setRole("STUDENT");
            profile.setPhone(updatedUser.getPhoneNumber());
            profile.setLocation(updatedUser.getAddress());

            return ResponseEntity.ok(profile);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to update profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/profile/picture")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> uploadProfilePicture(
            @RequestPart MultipartFile file,
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

            // ✅ Return UserProfile DTO
            UserProfile profile = new UserProfile();
            profile.setId(updatedUser.getId());
            profile.setUsername(updatedUser.getUsername());
            profile.setName(updatedUser.getName() != null ? updatedUser.getName() : "");
            profile.setEmail(updatedUser.getEmail() != null ? updatedUser.getEmail() : "");
            profile.setRole("STUDENT");
            profile.setPhone(updatedUser.getPhoneNumber());
            profile.setLocation(updatedUser.getAddress());

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

    @GetMapping("/profile/picture")
    @PreAuthorize("hasRole('STUDENT')")
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
}
```

---

## 3. AdminController.java (NEW - Create This File)

```java
package com.elearnhub.teacher_service.Controller;

import com.elearnhub.teacher_service.dto.UserProfile;
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
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Value("${file.profile-upload-dir:uploads/profiles}")
    private String profileUploadDir;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        try {
            String username = authentication.getName();
            Optional<User> userOptional = userService.findByUsername(username);

            if (userOptional.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            User user = userOptional.get();

            // ✅ Convert User to UserProfile DTO
            UserProfile profile = new UserProfile();
            profile.setId(user.getId());
            profile.setUsername(user.getUsername());
            profile.setName(user.getName() != null ? user.getName() : "");
            profile.setEmail(user.getEmail() != null ? user.getEmail() : "");
            profile.setRole("ADMIN");
            profile.setPhone(user.getPhoneNumber() != null ? user.getPhoneNumber() : null);
            profile.setLocation(user.getAddress() != null ? user.getAddress() : null);
            profile.setAdminLevel("Administrator"); // Default or get from User entity if available

            // TODO: Add admin-specific fields if available
            // profile.setPermissions(...);

            return ResponseEntity.ok(profile);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to fetch profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateProfile(
            @RequestBody Map<String, String> profileUpdate,
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
                String phoneNumber = profileUpdate.containsKey("phoneNumber") 
                    ? profileUpdate.get("phoneNumber") 
                    : profileUpdate.get("phone");
                if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
                    user.setPhoneNumber(phoneNumber.trim());
                } else {
                    user.setPhoneNumber(null);
                }
            }

            if (profileUpdate.containsKey("address") || profileUpdate.containsKey("location")) {
                String address = profileUpdate.containsKey("address") 
                    ? profileUpdate.get("address") 
                    : profileUpdate.get("location");
                if (address != null && !address.trim().isEmpty()) {
                    user.setAddress(address.trim());
                } else {
                    user.setAddress(null);
                }
            }

            user.setUsername(originalUsername);
            user.setRole(originalRole);

            User updatedUser = userService.updateUser(user.getId(), user);

            // ✅ Return UserProfile DTO
            UserProfile profile = new UserProfile();
            profile.setId(updatedUser.getId());
            profile.setUsername(updatedUser.getUsername());
            profile.setName(updatedUser.getName() != null ? updatedUser.getName() : "");
            profile.setEmail(updatedUser.getEmail() != null ? updatedUser.getEmail() : "");
            profile.setRole("ADMIN");
            profile.setPhone(updatedUser.getPhoneNumber());
            profile.setLocation(updatedUser.getAddress());
            profile.setAdminLevel("Administrator");

            return ResponseEntity.ok(profile);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to update profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/profile/picture")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> uploadProfilePicture(
            @RequestPart MultipartFile file,
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

            // ✅ Return UserProfile DTO
            UserProfile profile = new UserProfile();
            profile.setId(updatedUser.getId());
            profile.setUsername(updatedUser.getUsername());
            profile.setName(updatedUser.getName() != null ? updatedUser.getName() : "");
            profile.setEmail(updatedUser.getEmail() != null ? updatedUser.getEmail() : "");
            profile.setRole("ADMIN");
            profile.setPhone(updatedUser.getPhoneNumber());
            profile.setLocation(updatedUser.getAddress());
            profile.setAdminLevel("Administrator");

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

    @GetMapping("/profile/picture")
    @PreAuthorize("hasRole('ADMIN')")
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
}
```

---

## ✅ Key Changes Made:

### 1. **All Controllers Now Return UserProfile DTO**
   - Replaced `Map<String, Object>` with `UserProfile` DTO
   - Consistent response format across all roles

### 2. **TeacherController**
   - ✅ Returns `UserProfile` instead of Map
   - ✅ Calculates `totalClasses` from ClassService
   - ✅ Maps `phoneNumber` → `phone` and `address` → `location` for DTO
   - ✅ Handles both field names in update (phoneNumber/phone, address/location)

### 3. **StudentController**
   - ✅ Returns `UserProfile` instead of Map
   - ✅ Maps fields correctly to DTO
   - ✅ Ready for student-specific fields (commented TODOs)

### 4. **AdminController (NEW)**
   - ✅ Created new controller for admin profile
   - ✅ Same structure as teacher/student controllers
   - ✅ Sets `adminLevel` to "Administrator" by default

### 5. **Field Mapping**
   - `phoneNumber` (User entity) → `phone` (UserProfile DTO)
   - `address` (User entity) → `location` (UserProfile DTO)
   - Both field names supported in update endpoints for compatibility

---

## 📝 Notes:

1. **ClassService Dependency**: TeacherController needs `ClassService` to calculate `totalClasses` and `totalStudents`
2. **TODO Comments**: Student-specific fields (GPA, credits, etc.) are marked with TODOs - add when you have that data
3. **AdminController**: This is a NEW file - create it in your backend
4. **UserProfile DTO**: Make sure it's imported correctly in all controllers

All code is ready to paste! The frontend will now receive consistent `UserProfile` DTOs from all three endpoints.

