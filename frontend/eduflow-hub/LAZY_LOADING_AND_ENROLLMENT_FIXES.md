# 🔧 Fixes for Lazy Loading and Student Enrollment Checks

## Issues Found

1. **Lazy Loading Error**: `failed to lazily initialize a collection of role: com.elearnhub.teacher_service.entity.ClassEntity.students: could not initialize proxy - no Session`
2. **Student Enrollment Check Failing**: Students getting 403 even when enrolled in courses/classes
3. **Course/Assignment Access Denied**: Authorization checks not working correctly for students

---

## Fix 1: Lazy Loading Issue in `ClassController.getClassById()`

**Problem**: The `students` collection is being accessed after the transaction is closed.

**Solution**: Use `@Transactional` or fetch the collection within the transaction, or use JOIN FETCH in the query.

### Option A: Add `@Transactional` to the method (Recommended)

```java
@GetMapping("/{classId}")
@PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
@Transactional  // ✅ ADD THIS
public ResponseEntity<?> getClassById(
        @PathVariable Long classId,
        Authentication authentication) {
    try {
        String username = authentication.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<ClassEntity> classOptional = classService.getClassById(classId);
        if (classOptional.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Class not found with ID: " + classId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        ClassEntity classEntity = classOptional.get();

        // ✅ Authorization check
        if (user.getRole().equals("TEACHER")) {
            if (!classEntity.getTeacher().getId().equals(user.getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Unauthorized: Class does not belong to this teacher");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
        } else if (user.getRole().equals("STUDENT")) {
            // ✅ FIX: Initialize the collection within transaction
            // Force initialization by accessing size or iterating
            if (classEntity.getStudents() != null) {
                classEntity.getStudents().size(); // Force initialization
            }
            
            boolean isEnrolled = classEntity.getStudents() != null && 
                                classEntity.getStudents().stream()
                                    .anyMatch(student -> student.getId().equals(user.getId()));
            if (!isEnrolled) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Unauthorized: You are not enrolled in this class");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
        }

        // Convert to DTO
        ClassDTO classDTO = new ClassDTO();
        classDTO.setId(classEntity.getId());
        classDTO.setName(classEntity.getName());
        classDTO.setTeacherId(classEntity.getTeacher().getId());
        
        if (classEntity.getCourse() != null) {
            classDTO.setCourseId(classEntity.getCourse().getId());
        }
        
        // ✅ FIX: Initialize collection before accessing size
        if (classEntity.getStudents() != null) {
            classEntity.getStudents().size(); // Force initialization
            classDTO.setStudentCount(classEntity.getStudents().size());
        }

        return ResponseEntity.ok(classDTO);

    } catch (RuntimeException e) {
        Map<String, String> error = new HashMap<>();
        error.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    } catch (Exception e) {
        Map<String, String> error = new HashMap<>();
        error.put("message", "Failed to fetch class: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

**Required Import:**
```java
import org.springframework.transaction.annotation.Transactional;
```

---

## Fix 2: Student Enrollment Check in `CourseController.getCourseById()`

**Problem**: The student enrollment check might not be working correctly. The `getStudents()` collection might not be initialized or the check logic is wrong.

**Solution**: Ensure the collection is initialized and fix the enrollment check.

```java
@GetMapping("/{courseId}")
@PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
@Transactional  // ✅ ADD THIS to prevent lazy loading issues
public ResponseEntity<?> getCourseById(
        @PathVariable Long courseId,
        Authentication authentication) {
    try {
        String username = authentication.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<Course> courseOptional = courseService.getCourseById(courseId);
        if (courseOptional.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Course not found with ID: " + courseId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        Course course = courseOptional.get();
        
        // ✅ Allow access if:
        // 1. User is a teacher and owns the course, OR
        // 2. User is a student and is enrolled in the course
        if (user.getRole().equals("TEACHER")) {
            // Teacher must own the course
            if (!course.getTeacherId().equals(user.getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Unauthorized: Course does not belong to this teacher");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
        } else if (user.getRole().equals("STUDENT")) {
            // ✅ FIX: Initialize the collection and check enrollment
            if (course.getStudents() != null) {
                course.getStudents().size(); // Force initialization
            }
            
            boolean isEnrolled = course.getStudents() != null && 
                                 course.getStudents().stream()
                                     .anyMatch(student -> student.getId().equals(user.getId()));
            if (!isEnrolled) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Unauthorized: You are not enrolled in this course");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
        }

        return ResponseEntity.ok(course);

    } catch (RuntimeException e) {
        Map<String, String> error = new HashMap<>();
        error.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    } catch (Exception e) {
        Map<String, String> error = new HashMap<>();
        error.put("message", "Failed to fetch course: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

---

## Fix 3: Student Enrollment Check in `AssignmentController.getAssignmentById()`

**Problem**: Similar to the course check, the student enrollment verification is failing.

**Solution**: Ensure the collection is initialized and fix the enrollment check.

```java
@GetMapping("/{assignmentId}")
@PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
@Transactional  // ✅ ADD THIS to prevent lazy loading issues
public ResponseEntity<?> getAssignmentById(
        @PathVariable Long assignmentId,
        Authentication authentication) {
    try {
        String username = authentication.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<Assignment> assignmentOptional = assignmentRepository.findById(assignmentId);
        if (assignmentOptional.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Assignment not found with ID: " + assignmentId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        Assignment assignment = assignmentOptional.get();
        
        // Get the course for this assignment
        Optional<Course> courseOptional = courseService.getCourseById(assignment.getCourseId());
        if (courseOptional.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Course not found for assignment");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        Course course = courseOptional.get();

        // ✅ Authorization check:
        // - Teachers must own the course
        // - Students must be enrolled in the course
        if (user.getRole().equals("TEACHER")) {
            if (!course.getTeacherId().equals(user.getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Unauthorized: Assignment does not belong to this teacher");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
        } else if (user.getRole().equals("STUDENT")) {
            // ✅ FIX: Initialize the collection and check enrollment
            if (course.getStudents() != null) {
                course.getStudents().size(); // Force initialization
            }
            
            boolean isEnrolled = course.getStudents() != null && 
                                course.getStudents().stream()
                                    .anyMatch(student -> student.getId().equals(user.getId()));
            if (!isEnrolled) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Unauthorized: You are not enrolled in this course");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
        }

        // Convert to DTO
        AssignmentDTO assignmentDTO = assignmentService.convertToAssignmentDTO(assignment);
        return ResponseEntity.ok(assignmentDTO);

    } catch (RuntimeException e) {
        Map<String, String> error = new HashMap<>();
        error.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    } catch (Exception e) {
        Map<String, String> error = new HashMap<>();
        error.put("message", "Failed to fetch assignment: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

---

## Alternative Solution: Use JOIN FETCH in Repository Queries

If you prefer to fix this at the repository level, you can modify your repository queries to eagerly fetch the `students` collection:

### For `ClassEntityRepository`:

```java
@Query("SELECT DISTINCT c FROM ClassEntity c " +
       "LEFT JOIN FETCH c.students " +
       "LEFT JOIN FETCH c.course " +
       "LEFT JOIN FETCH c.teacher " +
       "WHERE c.id = :classId")
Optional<ClassEntity> findByIdWithStudents(@Param("classId") Long classId);
```

Then use this method in `ClassService.getClassById()`:

```java
public Optional<ClassEntity> getClassById(Long classId) {
    return classRepository.findByIdWithStudents(classId);
}
```

### For `CourseRepository`:

```java
@Query("SELECT DISTINCT c FROM Course c " +
       "LEFT JOIN FETCH c.students " +
       "WHERE c.id = :courseId")
Optional<Course> findByIdWithStudents(@Param("courseId") Long courseId);
```

---

## Summary

**Quick Fix (Recommended):**
1. Add `@Transactional` to all three methods (`getClassById`, `getCourseById`, `getAssignmentById`)
2. Force initialization of lazy collections by calling `.size()` before accessing them
3. Ensure student enrollment checks are done within the transaction

**Required Imports:**
```java
import org.springframework.transaction.annotation.Transactional;
```

After making these changes, the lazy loading errors should be resolved and student enrollment checks should work correctly.






