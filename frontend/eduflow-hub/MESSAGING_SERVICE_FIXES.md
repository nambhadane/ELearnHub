# Messaging System - Service Method Fixes

## Issues to Fix

1. Missing methods in `ClassService`:
   - `hasAccessToClass(Long classId, Long userId)`
   - `getClassStudents(Long classId)`

2. Missing method in `UserService`:
   - `getAllTeachers()`

3. **CRITICAL**: Business logic methods are incorrectly placed in `ClassDTO` - they should be in `ClassService`

4. `classEntityRepository` reference in `ClassDTO` - DTOs should not have repositories

---

## 1. Fix ClassDTO (Remove Business Logic)

**File:** `com.elearnhub.teacher_service.dto.ClassDTO`

**Remove these methods from ClassDTO:**
- `createClass()` - This belongs in `ClassService`
- `addStudentToClass()` - This belongs in `ClassService`

**Corrected ClassDTO:**
```java
package com.elearnhub.teacher_service.dto;

public class ClassDTO {
    private Long id;
    private String name;
    private Long teacherId;
    private Long courseId;

    public ClassDTO() {}

    public ClassDTO(Long id, String name, Long teacherId, Long courseId) {
        this.id = id;
        this.name = name;
        this.teacherId = teacherId;
        this.courseId = courseId;
    }

    public ClassDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters and setters only - NO business logic
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }
}
```

---

## 2. Add Missing Methods to ClassService

**File:** `com.elearnhub.teacher_service.service.ClassService`

**Add these methods to your ClassService interface:**

```java
// Add to ClassService interface
public interface ClassService {
    // ... existing methods ...
    
    /**
     * Check if user has access to a class
     * @param classId The class ID
     * @param userId The user ID
     * @return true if user is the teacher or an enrolled student
     */
    boolean hasAccessToClass(Long classId, Long userId);
    
    /**
     * Get all students enrolled in a class
     * @param classId The class ID
     * @return List of student DTOs
     */
    List<ParticipantDTO> getClassStudents(Long classId);
}
```

**Add these methods to your ClassServiceImpl:**

```java
package com.elearnhub.teacher_service.service;

import com.elearnhub.teacher_service.dto.ParticipantDTO;
import com.elearnhub.teacher_service.entity.ClassEntity;
import com.elearnhub.teacher_service.entity.User;
import com.elearnhub.teacher_service.repository.ClassEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClassServiceImpl implements ClassService {
    
    @Autowired
    private ClassEntityRepository classEntityRepository;
    
    // ... existing code ...
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasAccessToClass(Long classId, Long userId) {
        Optional<ClassEntity> classOpt = classEntityRepository.findById(classId);
        if (classOpt.isEmpty()) {
            return false;
        }
        
        ClassEntity classEntity = classOpt.get();
        
        // Check if user is the teacher
        if (classEntity.getTeacher() != null && 
            classEntity.getTeacher().getId().equals(userId)) {
            return true;
        }
        
        // Check if user is an enrolled student
        if (classEntity.getStudents() != null) {
            return classEntity.getStudents().stream()
                .anyMatch(student -> student.getId().equals(userId));
        }
        
        return false;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ParticipantDTO> getClassStudents(Long classId) {
        Optional<ClassEntity> classOpt = classEntityRepository.findById(classId);
        if (classOpt.isEmpty()) {
            throw new RuntimeException("Class not found");
        }
        
        ClassEntity classEntity = classOpt.get();
        
        if (classEntity.getStudents() == null || classEntity.getStudents().isEmpty()) {
            return new ArrayList<>();
        }
        
        return classEntity.getStudents().stream()
            .map(student -> {
                ParticipantDTO dto = new ParticipantDTO();
                dto.setId(student.getId());
                dto.setName(student.getName());
                dto.setUsername(student.getUsername());
                dto.setRole(student.getRole());
                // Add avatar if you have it in User entity
                // dto.setAvatar(student.getProfilePicture());
                return dto;
            })
            .collect(Collectors.toList());
    }
}
```

**Note:** Make sure you have the `ParticipantDTO` class. If not, add it:

```java
package com.elearnhub.teacher_service.dto;

public class ParticipantDTO {
    private Long id;
    private String name;
    private String username;
    private String role;
    private String avatar;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
```

---

## 3. Add Missing Method to UserService

**File:** `com.elearnhub.teacher_service.service.UserService`

**Add this method to your UserService interface:**

```java
// Add to UserService interface
public interface UserService {
    // ... existing methods ...
    
    /**
     * Get all users with TEACHER role
     * @return List of teacher DTOs
     */
    List<ParticipantDTO> getAllTeachers();
}
```

**Add this method to your UserServiceImpl:**

```java
package com.elearnhub.teacher_service.service;

import com.elearnhub.teacher_service.dto.ParticipantDTO;
import com.elearnhub.teacher_service.entity.User;
import com.elearnhub.teacher_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    // ... existing code ...
    
    @Override
    @Transactional(readOnly = true)
    public List<ParticipantDTO> getAllTeachers() {
        List<User> teachers = userRepository.findByRole("TEACHER");
        
        return teachers.stream()
            .map(teacher -> {
                ParticipantDTO dto = new ParticipantDTO();
                dto.setId(teacher.getId());
                dto.setName(teacher.getName());
                dto.setUsername(teacher.getUsername());
                dto.setRole(teacher.getRole());
                // Add avatar if you have it in User entity
                // dto.setAvatar(teacher.getProfilePicture());
                return dto;
            })
            .collect(Collectors.toList());
    }
}
```

**Make sure your UserRepository has this method:**

```java
package com.elearnhub.teacher_service.repository;

import com.elearnhub.teacher_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // ... existing methods ...
    
    List<User> findByRole(String role);
    
    Optional<User> findByUsername(String username);
}
```

---

## 4. Fix MessageController

**File:** `com.elearnhub.teacher_service.Controller.MessageController`

**Corrected MessageController with proper service injection:**

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

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private ClassService classService; // ✅ Add this

    // ... existing endpoints ...

    @GetMapping("/conversations/class/{classId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<?> getClassConversation(
            @PathVariable Long classId,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // ✅ Use ClassService method
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

    // Note: The endpoint below should be in ClassController, not MessageController
    // But if you want it in MessageController, use this:
}
```

**IMPORTANT:** The endpoint `GET /classes/{classId}/students` should be in **ClassController**, not MessageController. Add it to your ClassController:

```java
// In ClassController.java
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
        Optional<ClassEntity> classOpt = classService.getClassById(classId);
        if (classOpt.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Class not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        ClassEntity classEntity = classOpt.get();
        if (!classEntity.getTeacher().getId().equals(teacher.getId())) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Unauthorized: You don't own this class");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        // ✅ Use ClassService method
        List<ParticipantDTO> students = classService.getClassStudents(classId);
        return ResponseEntity.ok(students);
    } catch (Exception e) {
        Map<String, String> error = new HashMap<>();
        error.put("message", "Failed to fetch students: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

    @GetMapping("/teachers")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> getAllTeachers(Authentication authentication) {
        try {
            String username = authentication.getName();
            User currentTeacher = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Teacher not found"));

            // ✅ Use UserService method
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

**Add these imports to MessageController:**
```java
import com.elearnhub.teacher_service.service.ClassService;
import com.elearnhub.teacher_service.entity.ClassEntity;
import com.elearnhub.teacher_service.entity.User;
import java.util.Optional;
import java.util.stream.Collectors;
```

**Add these imports to ClassController (for the getClassStudents endpoint):**
```java
import com.elearnhub.teacher_service.dto.ParticipantDTO;
import com.elearnhub.teacher_service.entity.ClassEntity;
import com.elearnhub.teacher_service.entity.User;
import java.util.Optional;
```

---

## 5. Update ClassService to Auto-Create Conversations

**In your ClassService implementation, update the `createClass` method:**

```java
@Autowired
private MessageService messageService; // Add this dependency

@Override
@Transactional
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

**And update the `addStudentToClass` method (if you have one):**

```java
@Override
@Transactional
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

## Summary of Changes

1. ✅ **Remove business logic from ClassDTO** - DTOs should only have data fields
2. ✅ **Add `hasAccessToClass()` to ClassService** - Check if user can access a class
3. ✅ **Add `getClassStudents()` to ClassService** - Get students in a class
4. ✅ **Add `getAllTeachers()` to UserService** - Get all teachers
5. ✅ **Add ClassService injection to MessageController** - Fix the missing dependency
6. ✅ **Update ClassService to auto-create conversations** - When class is created
7. ✅ **Create ParticipantDTO** - If it doesn't exist

---

## Important Notes

- **DTOs should NEVER have business logic** - They are data transfer objects only
- **Repositories should NEVER be in DTOs** - Only in Service classes
- **Service classes handle all business logic** - Including auto-creation of conversations
- **Make sure to add `@Transactional`** - For methods that modify data
- **Handle exceptions gracefully** - Don't fail class creation if conversation creation fails

---

## Testing Checklist

After implementing these fixes:

1. ✅ Compile without errors
2. ✅ Test `hasAccessToClass()` - Teacher should have access, enrolled students should have access
3. ✅ Test `getClassStudents()` - Should return list of students in a class
4. ✅ Test `getAllTeachers()` - Should return all teachers except current user
5. ✅ Test class creation - Should auto-create conversation
6. ✅ Test student enrollment - Should auto-add to conversation
7. ✅ Test MessageController endpoints - Should work without errors

