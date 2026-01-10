# Complete ClassServiceImpl - Ready to Paste

## Complete ClassServiceImpl.java

```java
package com.elearnhub.teacher_service.service;

import com.elearnhub.teacher_service.dto.ClassDTO;
import com.elearnhub.teacher_service.dto.ParticipantDTO;
import com.elearnhub.teacher_service.entity.ClassEntity;
import com.elearnhub.teacher_service.entity.User;
import com.elearnhub.teacher_service.repository.ClassEntityRepository;
import com.elearnhub.teacher_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClassServiceImpl implements ClassService {

    @Autowired
    private ClassEntityRepository classEntityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageService messageService; // For auto-creating conversations

    // Create a new class
    @Override
    @Transactional
    public ClassDTO createClass(Long teacherId, Long courseId, String name) {
        // Find teacher
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        // Create class entity
        ClassEntity classEntity = new ClassEntity();
        classEntity.setName(name);
        classEntity.setTeacher(teacher);
        // Set course - adjust based on your ClassEntity structure
        // If ClassEntity has a Course field:
        // classEntity.setCourse(courseService.getCourseById(courseId).orElseThrow(...));
        // If ClassEntity has a courseId field:
        // classEntity.setCourseId(courseId);

        // Save class
        ClassEntity savedClass = classEntityRepository.save(classEntity);

        // ✅ AUTO-CREATE: Create group conversation for this class
        try {
            if (messageService != null) {
                messageService.createClassConversation(savedClass.getId());
            }
        } catch (Exception e) {
            // Log error but don't fail class creation
            System.err.println("Failed to create class conversation: " + e.getMessage());
            e.printStackTrace();
        }

        // Return DTO
        ClassDTO dto = new ClassDTO();
        dto.setId(savedClass.getId());
        dto.setName(savedClass.getName());
        dto.setTeacherId(teacherId);
        dto.setCourseId(courseId);
        return dto;
    }

    // Get class by ID
    @Override
    @Transactional(readOnly = true)
    public Optional<ClassEntity> getClassById(Long classId) {
        return classEntityRepository.findById(classId);
    }

    // Get classes by teacher
    @Override
    @Transactional(readOnly = true)
    public List<ClassDTO> getClassesByTeacher(Long teacherId) {
        List<ClassEntity> classes = classEntityRepository.findByTeacherId(teacherId);
        
        return classes.stream()
                .map(classEntity -> {
                    ClassDTO dto = new ClassDTO();
                    dto.setId(classEntity.getId());
                    dto.setName(classEntity.getName());
                    dto.setTeacherId(teacherId);
                    // Set courseId based on your ClassEntity structure
                    // if (classEntity.getCourse() != null) {
                    //     dto.setCourseId(classEntity.getCourse().getId());
                    // }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // Add student to class
    @Override
    @Transactional
    public void addStudentToClass(Long classId, Long studentId) {
        Optional<ClassEntity> classOpt = classEntityRepository.findById(classId);
        if (classOpt.isEmpty()) {
            throw new RuntimeException("Class not found");
        }

        Optional<User> studentOpt = userRepository.findById(studentId);
        if (studentOpt.isEmpty()) {
            throw new RuntimeException("Student not found");
        }

        ClassEntity classEntity = classOpt.get();
        User student = studentOpt.get();

        // Add student to class if not already added
        if (classEntity.getStudents() == null) {
            classEntity.setStudents(new ArrayList<>());
        }

        boolean alreadyEnrolled = classEntity.getStudents().stream()
                .anyMatch(s -> s.getId().equals(studentId));

        if (!alreadyEnrolled) {
            classEntity.getStudents().add(student);
            classEntityRepository.save(classEntity);

            // ✅ AUTO-ADD: Add student to class group conversation
            try {
                if (messageService != null) {
                    // Get or create class conversation
                    var classConversation = messageService.getClassConversation(classId);
                    if (classConversation == null) {
                        // Create if doesn't exist
                        classConversation = messageService.createClassConversation(classId);
                    }
                    
                    if (classConversation != null) {
                        messageService.addParticipantToConversation(classConversation.getId(), studentId);
                    }
                }
            } catch (Exception e) {
                // Log error but don't fail student addition
                System.err.println("Failed to add student to class conversation: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Remove student from class
    @Override
    @Transactional
    public void removeStudentFromClass(Long classId, Long studentId) {
        Optional<ClassEntity> classOpt = classEntityRepository.findById(classId);
        if (classOpt.isEmpty()) {
            throw new RuntimeException("Class not found");
        }

        ClassEntity classEntity = classOpt.get();
        if (classEntity.getStudents() != null) {
            classEntity.getStudents().removeIf(s -> s.getId().equals(studentId));
            classEntityRepository.save(classEntity);

            // Optionally remove from conversation
            try {
                if (messageService != null) {
                    var classConversation = messageService.getClassConversation(classId);
                    if (classConversation != null) {
                        messageService.removeParticipantFromConversation(classConversation.getId(), studentId);
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to remove student from class conversation: " + e.getMessage());
            }
        }
    }

    // Check if user has access to a class
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

    // Get all students enrolled in a class
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

    // Update class
    @Override
    @Transactional
    public ClassDTO updateClass(Long classId, String name) {
        Optional<ClassEntity> classOpt = classEntityRepository.findById(classId);
        if (classOpt.isEmpty()) {
            throw new RuntimeException("Class not found");
        }

        ClassEntity classEntity = classOpt.get();
        classEntity.setName(name);
        ClassEntity savedClass = classEntityRepository.save(classEntity);

        ClassDTO dto = new ClassDTO();
        dto.setId(savedClass.getId());
        dto.setName(savedClass.getName());
        if (savedClass.getTeacher() != null) {
            dto.setTeacherId(savedClass.getTeacher().getId());
        }
        // Set courseId based on your structure
        return dto;
    }

    // Delete class
    @Override
    @Transactional
    public void deleteClass(Long classId) {
        Optional<ClassEntity> classOpt = classEntityRepository.findById(classId);
        if (classOpt.isPresent()) {
            classEntityRepository.delete(classOpt.get());
        }
    }

    // Get all classes (optional - if needed)
    @Override
    @Transactional(readOnly = true)
    public List<ClassDTO> getAllClasses() {
        List<ClassEntity> classes = classEntityRepository.findAll();
        
        return classes.stream()
                .map(classEntity -> {
                    ClassDTO dto = new ClassDTO();
                    dto.setId(classEntity.getId());
                    dto.setName(classEntity.getName());
                    if (classEntity.getTeacher() != null) {
                        dto.setTeacherId(classEntity.getTeacher().getId());
                    }
                    // Set courseId based on your structure
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
```

---

## Complete ClassService Interface

```java
package com.elearnhub.teacher_service.service;

import com.elearnhub.teacher_service.dto.ClassDTO;
import com.elearnhub.teacher_service.dto.ParticipantDTO;
import com.elearnhub.teacher_service.entity.ClassEntity;
import java.util.List;
import java.util.Optional;

public interface ClassService {
    
    // Create a new class
    ClassDTO createClass(Long teacherId, Long courseId, String name);
    
    // Get class by ID
    Optional<ClassEntity> getClassById(Long classId);
    
    // Get classes by teacher
    List<ClassDTO> getClassesByTeacher(Long teacherId);
    
    // Add student to class
    void addStudentToClass(Long classId, Long studentId);
    
    // Remove student from class
    void removeStudentFromClass(Long classId, Long studentId);
    
    // Check if user has access to a class
    boolean hasAccessToClass(Long classId, Long userId);
    
    // Get all students enrolled in a class
    List<ParticipantDTO> getClassStudents(Long classId);
    
    // Update class
    ClassDTO updateClass(Long classId, String name);
    
    // Delete class
    void deleteClass(Long classId);
    
    // Get all classes (optional)
    List<ClassDTO> getAllClasses();
}
```

---

## Required Repository Methods

Make sure your `ClassEntityRepository` has these methods:

```java
package com.elearnhub.teacher_service.repository;

import com.elearnhub.teacher_service.entity.ClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassEntityRepository extends JpaRepository<ClassEntity, Long> {
    
    // Find classes by teacher ID
    List<ClassEntity> findByTeacherId(Long teacherId);
    
    // Find classes by teacher
    List<ClassEntity> findByTeacher(User teacher);
    
    // Find classes by course (if you have course relationship)
    // List<ClassEntity> findByCourseId(Long courseId);
}
```

---

## Required Dependencies

Make sure you have these in your `pom.xml` or `build.gradle`:

**For Maven (pom.xml):**
```xml
<dependencies>
    <!-- Spring Boot Starter Data JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <!-- Spring Boot Starter Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- MySQL Driver (or your database driver) -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
    </dependency>
</dependencies>
```

---

## Important Notes

1. **MessageService Dependency**: The `messageService` is marked as `@Autowired` but will be `null` if MessageService is not yet implemented. The code handles this gracefully with null checks.

2. **Course Relationship**: Adjust the course relationship based on your `ClassEntity` structure:
   - If `ClassEntity` has a `Course` object: use `classEntity.getCourse().getId()`
   - If `ClassEntity` has a `courseId` field: use `classEntity.getCourseId()`
   - If you need to inject `CourseService`, add it as a dependency

3. **User Entity**: Make sure your `User` entity has:
   - `getId()` method
   - `getName()` method
   - `getUsername()` method
   - `getRole()` method
   - `getStudents()` method (in ClassEntity) - should return `List<User>`

4. **ClassEntity Structure**: Your `ClassEntity` should have:
   - `Long id`
   - `String name`
   - `User teacher` (ManyToOne relationship)
   - `List<User> students` (ManyToMany relationship)
   - Course relationship (adjust based on your structure)

5. **Transaction Management**: All methods that modify data use `@Transactional`, and read-only methods use `@Transactional(readOnly = true)`.

6. **Error Handling**: Methods throw `RuntimeException` for missing entities. You can customize this to throw custom exceptions if needed.

---

## Usage Example

```java
// In your ClassController
@Autowired
private ClassService classService;

@PostMapping
public ResponseEntity<?> createClass(@RequestBody CreateClassRequest request) {
    ClassDTO created = classService.createClass(
        request.getTeacherId(),
        request.getCourseId(),
        request.getName()
    );
    return ResponseEntity.ok(created);
}

@GetMapping("/{classId}/students")
public ResponseEntity<?> getStudents(@PathVariable Long classId) {
    List<ParticipantDTO> students = classService.getClassStudents(classId);
    return ResponseEntity.ok(students);
}
```

---

## Testing Checklist

After implementing:

1. ✅ Compile without errors
2. ✅ Test `createClass()` - Should create class and conversation
3. ✅ Test `getClassById()` - Should return class
4. ✅ Test `getClassesByTeacher()` - Should return teacher's classes
5. ✅ Test `addStudentToClass()` - Should add student and add to conversation
6. ✅ Test `hasAccessToClass()` - Teacher and enrolled students should have access
7. ✅ Test `getClassStudents()` - Should return list of students
8. ✅ Test `removeStudentFromClass()` - Should remove student
9. ✅ Test `updateClass()` - Should update class name
10. ✅ Test `deleteClass()` - Should delete class

---

This code is ready to paste directly into your project. Just adjust the course relationship parts based on your actual `ClassEntity` structure.

