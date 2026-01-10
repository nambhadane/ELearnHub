# Backend Requirements for "Add Student to Class" Feature

## Summary
The frontend now has a UI for teachers to add students to classes. A backend endpoint is needed to fetch all students.

## Required Backend Endpoint

### Get All Students

**Endpoint:** `GET /students`  
**Authorization:** `@PreAuthorize("hasRole('TEACHER')")`  
**Response:** `List<ParticipantDTO>` or `List<UserDTO>`

---

## Implementation

### Option 1: Add to StudentController (Recommended)

```java
package com.elearnhub.teacher_service.Controller;

import com.elearnhub.teacher_service.dto.ParticipantDTO;
import com.elearnhub.teacher_service.entity.User;
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
@RequestMapping("/student")
public class StudentController {
    
    @Autowired
    private UserService userService;
    
    // ... existing endpoints ...
    
    @GetMapping("/students")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> getAllStudents(Authentication authentication) {
        try {
            String username = authentication.getName();
            User teacher = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Teacher not found"));

            // Get all students (users with role STUDENT)
            List<ParticipantDTO> students = userService.getAllStudents();
            
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to fetch students: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
```

**Note:** The endpoint is `/students` (plural) to get all students, not `/student` (singular) which is for the authenticated student's profile.

---

### Option 2: Add to TeacherController

```java
// In TeacherController.java
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
```

**Note:** If added to TeacherController, the URL will be `/teacher/students`. You'll need to update the frontend API call accordingly.

---

## Required Service Method

Add this method to `UserService`:

```java
List<ParticipantDTO> getAllStudents();
```

---

## Required Service Implementation

Add this method to `UserServiceImpl` (or `UserService` if it's a class):

```java
@Transactional(readOnly = true)
public List<ParticipantDTO> getAllStudents() {
    List<User> students = userRepository.findByRole("STUDENT");

    return students.stream()
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
```

---

## Required Repository Method

Make sure your `UserRepository` has this method:

```java
package com.elearnhub.teacher_service.repository;

import com.elearnhub.teacher_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // ... existing methods ...
    
    // Find all users with STUDENT role
    List<User> findByRole(String role);
    
    // ... other methods ...
}
```

---

## ParticipantDTO Structure

Make sure your `ParticipantDTO` has these fields:

```java
package com.elearnhub.teacher_service.dto;

public class ParticipantDTO {
    private Long id;
    private String name;
    private String username;
    private String role;
    private String avatar; // Optional
    
    // Constructors, getters, setters
}
```

---

## Frontend API Integration

The frontend is already set up to call:
- `GET /students` - Get all students
- `POST /classes/{classId}/students?studentId={id}` - Add student to class (already exists)
- `GET /classes/{classId}/students` - Get students in class (already exists)

---

## Summary

1. ✅ Add `getAllStudents()` method to `UserService` interface/class
2. ✅ Implement the method in `UserServiceImpl` (or `UserService`)
3. ✅ Add repository method `findByRole(String role)` to `UserRepository` (if not already exists)
4. ✅ Add endpoint `GET /students` (or `/teacher/students`) to `StudentController` (or `TeacherController`)
5. ✅ Ensure proper authentication and authorization

---

## Testing

After implementation, test:
- [ ] Teacher can fetch all students
- [ ] Only students (not teachers/admins) are returned
- [ ] Empty list returned if no students exist
- [ ] Proper error handling for unauthorized access
- [ ] Students can be added to classes successfully

---

## Alternative: If you already have a similar endpoint

If you already have an endpoint that returns all users or students, you can:
1. Reuse that endpoint
2. Update the frontend API call in `api.ts` to use your existing endpoint
3. Make sure it returns data in `ParticipantDTO` format

The frontend expects:
```typescript
interface ParticipantDTO {
  id: number;
  name?: string;
  username: string;
  role?: string;
  avatar?: string;
}
```






