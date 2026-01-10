# Backend Requirements for Student "My Classes" Feature

## Summary
The frontend needs an endpoint to fetch all classes that a student is enrolled in.

## Required Backend Endpoint

### Get Classes by Student ID

**Endpoint:** `GET /classes/student/{studentId}`  
**Alternative (Recommended):** `GET /student/classes` (uses authentication, more secure)

**Authorization:** `@PreAuthorize("hasRole('STUDENT')")`  
**Response:** `List<ClassDTO>` or `List<StudentClassDTO>` (if you want to include additional student-specific info)

---

## Option 1: Using Student ID in Path (Current Pattern)

```java
@GetMapping("/classes/student/{studentId}")
@PreAuthorize("hasRole('STUDENT')")
public ResponseEntity<List<ClassDTO>> getClassesByStudent(@PathVariable Long studentId, Authentication authentication) {
    // Verify the authenticated student matches the requested studentId
    String username = authentication.getName();
    User currentUser = userService.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
    
    if (!currentUser.getId().equals(studentId)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    
    List<ClassDTO> classes = classService.getClassesByStudent(studentId);
    return ResponseEntity.ok(classes);
}
```

---

## Option 2: Using Authentication Only (Recommended - More Secure)

```java
@GetMapping("/student/classes")
@PreAuthorize("hasRole('STUDENT')")
public ResponseEntity<List<ClassDTO>> getMyClasses(Authentication authentication) {
    String username = authentication.getName();
    User student = userService.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Student not found"));
    
    List<ClassDTO> classes = classService.getClassesByStudent(student.getId());
    return ResponseEntity.ok(classes);
}
```

---

## Required Service Method

Add this method to `ClassService` interface:

```java
List<ClassDTO> getClassesByStudent(Long studentId);
```

---

## Required Service Implementation

Add this method to `ClassServiceImpl`:

```java
@Override
@Transactional(readOnly = true)
public List<ClassDTO> getClassesByStudent(Long studentId) {
    // Find all classes where this student is enrolled
    List<ClassEntity> classes = classEntityRepository.findByStudents_Id(studentId);
    
    return classes.stream()
            .map(classEntity -> {
                ClassDTO dto = new ClassDTO();
                dto.setId(classEntity.getId());
                dto.setName(classEntity.getName());
                dto.setTeacherId(classEntity.getTeacher().getId());
                dto.setCourseId(classEntity.getCourse().getId());
                return dto;
            })
            .collect(Collectors.toList());
}
```

---

## Required Repository Method

Add this method to `ClassEntityRepository`:

```java
package com.elearnhub.teacher_service.repository;

import com.elearnhub.teacher_service.entity.ClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ClassEntityRepository extends JpaRepository<ClassEntity, Long> {
    // ... existing methods ...
    
    // Find classes where a specific student is enrolled
    @Query("SELECT c FROM ClassEntity c JOIN c.students s WHERE s.id = :studentId")
    List<ClassEntity> findByStudents_Id(@Param("studentId") Long studentId);
    
    // OR using Spring Data JPA method naming (if your relationship is set up correctly):
    // List<ClassEntity> findByStudentsId(Long studentId);
}
```

---

## Enhanced DTO (Optional - If you want student-specific info)

If you want to include additional information like enrollment date, progress, etc., create a `StudentClassDTO`:

```java
package com.elearnhub.teacher_service.dto;

public class StudentClassDTO {
    private Long id;
    private String name;
    private Long teacherId;
    private String teacherName;
    private Long courseId;
    private String courseName;
    private Integer studentCount;
    private String enrollmentDate; // When student was added
    // Add more fields as needed
    
    // Constructors, getters, setters
}
```

---

## Controller Location

You can add this endpoint to:
1. **ClassController** - If you want `/classes/student/{studentId}`
2. **StudentController** - If you want `/student/classes` (recommended)

---

## Example: StudentController Implementation

```java
package com.elearnhub.teacher_service.Controller;

import com.elearnhub.teacher_service.dto.ClassDTO;
import com.elearnhub.teacher_service.entity.User;
import com.elearnhub.teacher_service.service.ClassService;
import com.elearnhub.teacher_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/student")
public class StudentController {
    
    @Autowired
    private ClassService classService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/classes")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<ClassDTO>> getMyClasses(Authentication authentication) {
        try {
            String username = authentication.getName();
            User student = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Student not found"));
            
            List<ClassDTO> classes = classService.getClassesByStudent(student.getId());
            return ResponseEntity.ok(classes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
```

---

## Database Relationship

Make sure your `ClassEntity` has a proper relationship with `User` (students):

```java
@Entity
public class ClassEntity {
    // ... existing fields ...
    
    @ManyToMany
    @JoinTable(
        name = "class_student",
        joinColumns = @JoinColumn(name = "class_id"),
        inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private Set<User> students = new HashSet<>();
    
    // ... rest of the entity ...
}
```

---

## Summary

1. ✅ Add `getClassesByStudent(Long studentId)` method to `ClassService` interface
2. ✅ Implement the method in `ClassServiceImpl`
3. ✅ Add repository method `findByStudents_Id(Long studentId)` to `ClassEntityRepository`
4. ✅ Add endpoint `GET /student/classes` (or `/classes/student/{studentId}`) to `StudentController` (or `ClassController`)
5. ✅ Ensure proper authentication and authorization

---

## Testing

After implementation, test:
- [ ] Student can fetch their enrolled classes
- [ ] Student cannot see classes they're not enrolled in
- [ ] Empty list returned if student has no classes
- [ ] Proper error handling for invalid student ID






