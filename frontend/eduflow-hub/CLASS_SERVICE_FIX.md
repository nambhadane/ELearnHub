# ClassService - Add Missing getClassById Method

## Problem
`AssignmentController` calls `classService.getClassById(classId)` but this method doesn't exist in `ClassService`.

## Solution
Add the `getClassById` method to your `ClassService.java`:

```java
package com.elearnhub.teacher_service.service;

import com.elearnhub.teacher_service.dto.ClassDTO;
import com.elearnhub.teacher_service.entity.ClassEntity;
import com.elearnhub.teacher_service.entity.Course;
import com.elearnhub.teacher_service.entity.User;
import com.elearnhub.teacher_service.repository.ClassEntityRepository;
import com.elearnhub.teacher_service.repository.CourseRepository;
import com.elearnhub.teacher_service.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClassService {

    @Autowired
    private ClassEntityRepository classEntityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    public ClassDTO createClass(Long teacherId, Long courseId, String name) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        ClassEntity classEntity = new ClassEntity(name, teacher, course);
        ClassEntity savedClass = classEntityRepository.save(classEntity);

        return new ClassDTO(savedClass.getId(), savedClass.getName(), teacherId, courseId);
    }

    public List<ClassDTO> getClassesByTeacher(Long teacherId) {
        List<ClassEntity> classes = classEntityRepository.findByTeacherId(teacherId);
        return classes.stream()
                .map(classEntity -> new ClassDTO(
                        classEntity.getId(),
                        classEntity.getName(),
                        classEntity.getTeacher().getId(),
                        classEntity.getCourse().getId()
                ))
                .collect(Collectors.toList());
    }

    // ✅ ADD THIS METHOD
    public Optional<ClassEntity> getClassById(Long classId) {
        return classEntityRepository.findById(classId);
    }

    public void addStudentToClass(Long classId, Long studentId) {
        ClassEntity classEntity = classEntityRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        classEntity.addStudent(student);
        classEntityRepository.save(classEntity);
    }

    public List<ClassDTO> getClassesForStudent(Long studentId) {
        return classEntityRepository.findAll().stream()
                .filter(c -> c.getStudents().stream().anyMatch(s -> s.getId().equals(studentId)))
                .map(c -> new ClassDTO(c.getId(), c.getName()))
                .collect(Collectors.toList());
    }
}
```

## What Changed:
1. ✅ Added `import java.util.Optional;` at the top
2. ✅ Added `getClassById(Long classId)` method that returns `Optional<ClassEntity>`

This method will work with the `AssignmentController` code that uses:
```java
ClassEntity classEntity = classService.getClassById(assignmentDTO.getClassId())
    .orElseThrow(() -> new RuntimeException("Class not found"));
```

---

## Alternative: If you prefer to throw exception directly

If you want the method to throw exception directly (instead of returning Optional), you can use:

```java
public ClassEntity getClassById(Long classId) {
    return classEntityRepository.findById(classId)
            .orElseThrow(() -> new RuntimeException("Class not found"));
}
```

Then in `AssignmentController`, you would change:
```java
// From:
ClassEntity classEntity = classService.getClassById(assignmentDTO.getClassId())
    .orElseThrow(() -> new RuntimeException("Class not found"));

// To:
ClassEntity classEntity = classService.getClassById(assignmentDTO.getClassId());
```

**Recommendation:** Use the `Optional` version (first option) as it's more flexible and follows Spring Data JPA patterns.

