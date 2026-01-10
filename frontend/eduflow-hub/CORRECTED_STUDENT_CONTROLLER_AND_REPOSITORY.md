# Corrected StudentController and ClassEntityRepository

## Issue 1: StudentController Missing ClassService

**Problem:** `getMyClasses()` calls `classService.getClassesByStudent()` but `classService` is not autowired.

## Issue 2: ClassEntityRepository Query Error

**Problem:** Query references `ClassStudent` entity which doesn't exist. Should use the `@ManyToMany` relationship directly.

---

## ✅ Corrected StudentController

```java
package com.elearnhub.teacher_service.Controller;

import com.elearnhub.teacher_service.dto.AssignmentDTO;
import com.elearnhub.teacher_service.dto.ClassDTO;
import com.elearnhub.teacher_service.dto.ParticipantDTO;
import com.elearnhub.teacher_service.dto.SubmissionDTO;
import com.elearnhub.teacher_service.dto.UserProfile;
import com.elearnhub.teacher_service.entity.User;
import com.elearnhub.teacher_service.entity.Course;
import com.elearnhub.teacher_service.service.UserService;
import com.elearnhub.teacher_service.service.CourseService;
import com.elearnhub.teacher_service.service.AssignmentService;
import com.elearnhub.teacher_service.service.ClassService;  // ✅ ADD THIS IMPORT
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

    @Autowired
    private ClassService classService;  // ✅ ADD THIS - CRITICAL!

    @Value("${file.profile-upload-dir:uploads/profiles}")
    private String profileUploadDir;

    @GetMapping("/classes")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getMyClasses(Authentication authentication) {
        try {
            String username = authentication.getName();
            User student = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            // ✅ Get CLASSES, not courses
            List<ClassDTO> classes = classService.getClassesByStudent(student.getId());
            
            return ResponseEntity.ok(classes);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to fetch classes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ... rest of your methods remain the same ...
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

            UserProfile profile = new UserProfile();
            profile.setId(user.getId());
            profile.setUsername(user.getUsername());
            profile.setName(user.getName() != null ? user.getName() : "");
            profile.setEmail(user.getEmail() != null ? user.getEmail() : "");
            profile.setRole("STUDENT");
            profile.setPhone(user.getPhoneNumber() != null ? user.getPhoneNumber() : null);
            profile.setLocation(user.getAddress() != null ? user.getAddress() : null);

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

## ✅ Corrected ClassEntityRepository

```java
package com.elearnhub.teacher_service.repository;

import com.elearnhub.teacher_service.entity.ClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ClassEntityRepository extends JpaRepository<ClassEntity, Long> {

    List<ClassEntity> findByTeacherId(Long teacherId);

    @Query("SELECT DISTINCT c FROM ClassEntity c " +
           "LEFT JOIN FETCH c.students " +
           "WHERE c.teacher.id = :teacherId")
    List<ClassEntity> findByTeacherIdWithStudents(@Param("teacherId") Long teacherId);

    // ✅ CORRECTED: Use the @ManyToMany relationship directly, not a non-existent ClassStudent entity
    @Query("SELECT DISTINCT c FROM ClassEntity c " +
           "JOIN c.students s " +
           "WHERE s.id = :studentId")
    List<ClassEntity> findByStudents_Id(@Param("studentId") Long studentId);
}
```

---

## Summary of Changes

1. ✅ **Added `@Autowired private ClassService classService;`** in `StudentController`
2. ✅ **Added import for `ClassService`** in `StudentController`
3. ✅ **Fixed `findByStudents_Id()` query** - Removed reference to non-existent `ClassStudent` entity
4. ✅ **Used direct JOIN on `c.students`** relationship instead

The key fix is:
- **Before:** Query tried to use `ClassStudent` entity (doesn't exist)
- **After:** Query uses `JOIN c.students s WHERE s.id = :studentId` (uses the actual `@ManyToMany` relationship)

This should now work correctly!






