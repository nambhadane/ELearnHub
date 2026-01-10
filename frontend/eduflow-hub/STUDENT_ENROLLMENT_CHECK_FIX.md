# 🔧 Fix for Student Enrollment Check Logic

## Problem

Students are getting **403 Forbidden** errors even though they're enrolled in classes. The issue is:

- **Students are enrolled in CLASSES** (via `class_student` table)
- **NOT directly in COURSES** (via `course_student` table)
- The enrollment check only looks at `course.getStudents()` which checks `course_student` table
- But students should be able to access a course if they're enrolled in ANY class that belongs to that course

## Solution

Update the enrollment check to allow access if:
1. Student is directly enrolled in the course (via `course_student`), OR
2. Student is enrolled in any class that belongs to this course (via `class_student` and `class_entity`)

---

## Fix 1: Update `CourseController.getCourseById()`

**Current Issue**: Only checks `course.getStudents()` which looks at `course_student` table.

**Fix**: Also check if student is enrolled in any class that uses this course.

```java
@GetMapping("/{courseId}")
@PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
@Transactional
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
        // 2. User is a student and is enrolled in the course (directly OR via a class)
        if (user.getRole().equals("TEACHER")) {
            // Teacher must own the course
            if (!course.getTeacherId().equals(user.getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Unauthorized: Course does not belong to this teacher");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
        } else if (user.getRole().equals("STUDENT")) {
            // ✅ FIX: Check both direct enrollment AND enrollment via classes
            boolean isEnrolled = false;
            
            // Option 1: Check direct enrollment in course
            if (course.getStudents() != null) {
                course.getStudents().size(); // Force initialization
                isEnrolled = course.getStudents().stream()
                    .anyMatch(student -> student.getId().equals(user.getId()));
            }
            
            // Option 2: Check if student is enrolled in any class that uses this course
            if (!isEnrolled) {
                // Use ClassService to check if student is in any class for this course
                List<ClassDTO> studentClasses = classService.getClassesByStudent(user.getId());
                isEnrolled = studentClasses.stream()
                    .anyMatch(classDTO -> classDTO.getCourseId() != null && 
                                         classDTO.getCourseId().equals(courseId));
            }
            
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

**Required Import:**
```java
import com.elearnhub.teacher_service.service.ClassService;
import com.elearnhub.teacher_service.dto.ClassDTO;
import java.util.List;
```

**Also add to CourseController:**
```java
@Autowired
private ClassService classService;  // ✅ ADD THIS
```

---

## Fix 2: Update `AssignmentController.getAssignmentById()`

**Same issue**: Only checks course enrollment, not class enrollment.

```java
@GetMapping("/{assignmentId}")
@PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
@Transactional
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
        // - Students must be enrolled in the course (directly OR via a class)
        if (user.getRole().equals("TEACHER")) {
            if (!course.getTeacherId().equals(user.getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Unauthorized: Assignment does not belong to this teacher");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
        } else if (user.getRole().equals("STUDENT")) {
            // ✅ FIX: Check both direct enrollment AND enrollment via classes
            boolean isEnrolled = false;
            
            // Option 1: Check direct enrollment in course
            if (course.getStudents() != null) {
                course.getStudents().size(); // Force initialization
                isEnrolled = course.getStudents().stream()
                    .anyMatch(student -> student.getId().equals(user.getId()));
            }
            
            // Option 2: Check if student is enrolled in any class that uses this course
            if (!isEnrolled) {
                List<ClassDTO> studentClasses = classService.getClassesByStudent(user.getId());
                isEnrolled = studentClasses.stream()
                    .anyMatch(classDTO -> classDTO.getCourseId() != null && 
                                         classDTO.getCourseId().equals(course.getId()));
            }
            
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

**Required Import:**
```java
import com.elearnhub.teacher_service.service.ClassService;
import com.elearnhub.teacher_service.dto.ClassDTO;
import java.util.List;
```

**Also add to AssignmentController:**
```java
@Autowired
private ClassService classService;  // ✅ ADD THIS (if not already present)
```

---

## Alternative: More Efficient Query-Based Check

If you want a more efficient solution, you can create a repository method to check enrollment directly:

### Add to `ClassEntityRepository`:

```java
@Query("SELECT COUNT(c) > 0 FROM ClassEntity c " +
       "JOIN c.students s " +
       "WHERE c.course.id = :courseId AND s.id = :studentId")
boolean isStudentEnrolledInCourse(@Param("courseId") Long courseId, 
                                   @Param("studentId") Long studentId);
```

### Then use it in controllers:

```java
// In CourseController.getCourseById() for students:
if (user.getRole().equals("STUDENT")) {
    boolean isEnrolled = false;
    
    // Check direct enrollment
    if (course.getStudents() != null) {
        course.getStudents().size();
        isEnrolled = course.getStudents().stream()
            .anyMatch(student -> student.getId().equals(user.getId()));
    }
    
    // Check enrollment via classes
    if (!isEnrolled) {
        isEnrolled = classRepository.isStudentEnrolledInCourse(courseId, user.getId());
    }
    
    if (!isEnrolled) {
        Map<String, String> error = new HashMap<>();
        error.put("message", "Unauthorized: You are not enrolled in this course");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
}
```

---

## Summary

The key insight is:
- **Students are enrolled in CLASSES**, not directly in courses
- **Classes belong to COURSES**
- So students should have access to a course if they're enrolled in ANY class that uses that course

After implementing these fixes, students will be able to:
- ✅ Access courses they're enrolled in (via classes)
- ✅ Access assignments from those courses
- ✅ View class details

The enrollment check now considers both:
1. Direct course enrollment (`course_student` table)
2. Indirect enrollment via classes (`class_student` + `class_entity` tables)






