# 🔧 Backend Fixes for Student Access to Classes, Courses, and Assignments

## Issues Found

1. **`GET /courses/{courseId}` returns 403** - Students cannot access courses they're enrolled in
2. **`GET /classes/{classId}` returns 404** - No endpoint for students to get class details
3. **`GET /assignments/{assignmentId}` returns 405** - No endpoint to get assignment by ID

---

## Fix 1: Update `CourseController.getCourseById()` to Allow Students

**Location:** `CourseController.java`

**Current Issue:** Only allows teachers who own the course. Students need access to courses they're enrolled in.

**Fix:**

```java
@GetMapping("/{courseId}")
@PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
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
            // Student must be enrolled in the course
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

**Key Changes:**
- Changed `@PreAuthorize("hasRole('TEACHER')")` to `@PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")`
- Added logic to check if student is enrolled in the course
- Teachers still must own the course

---

## Fix 2: Add `GET /classes/{classId}` Endpoint for Students

**Location:** `ClassController.java`

**Current Issue:** No endpoint exists for students to get class details.

**Fix:**

Add this method to your `ClassController`:

```java
@GetMapping("/{classId}")
@PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
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

        // ✅ Authorization check:
        // - Teachers must own the class
        // - Students must be enrolled in the class
        if (user.getRole().equals("TEACHER")) {
            if (!classEntity.getTeacherId().equals(user.getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Unauthorized: Class does not belong to this teacher");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
        } else if (user.getRole().equals("STUDENT")) {
            // Check if student is enrolled in this class
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
        classDTO.setTeacherId(classEntity.getTeacherId());
        if (classEntity.getCourse() != null) {
            classDTO.setCourseId(classEntity.getCourse().getId());
        }
        // Set student count if needed
        if (classEntity.getStudents() != null) {
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

**Note:** Make sure your `ClassService.getClassById()` returns `Optional<ClassEntity>`, not `Optional<ClassDTO>`. If it returns `ClassDTO`, adjust the code accordingly.

---

## Fix 3: Add `GET /assignments/{assignmentId}` Endpoint

**Location:** `AssignmentController.java`

**Current Issue:** No endpoint exists to get a single assignment by ID.

**Fix:**

Add this method to your `AssignmentController`:

```java
@GetMapping("/{assignmentId}")
@PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
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
            // Check if student is enrolled in the course
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

**Alternative:** If your `AssignmentService` already has a `getAssignmentById()` method, use it:

```java
@GetMapping("/{assignmentId}")
@PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
public ResponseEntity<?> getAssignmentById(
        @PathVariable Long assignmentId,
        Authentication authentication) {
    try {
        String username = authentication.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Use service method if available
        AssignmentDTO assignmentDTO = assignmentService.getAssignmentById(assignmentId);
        
        // Get course for authorization check
        Optional<Course> courseOptional = courseService.getCourseById(assignmentDTO.getCourseId());
        if (courseOptional.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Course not found for assignment");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        Course course = courseOptional.get();

        // Authorization check
        if (user.getRole().equals("TEACHER")) {
            if (!course.getTeacherId().equals(user.getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Unauthorized: Assignment does not belong to this teacher");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
        } else if (user.getRole().equals("STUDENT")) {
            boolean isEnrolled = course.getStudents() != null && 
                                course.getStudents().stream()
                                    .anyMatch(student -> student.getId().equals(user.getId()));
            if (!isEnrolled) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Unauthorized: You are not enrolled in this course");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
        }

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

## Summary

After implementing these three fixes:

1. ✅ Students can access courses they're enrolled in (`GET /courses/{courseId}`)
2. ✅ Students can access classes they're enrolled in (`GET /classes/{classId}`)
3. ✅ Students and teachers can access assignments (`GET /assignments/{assignmentId}`)

All endpoints now properly check authorization:
- **Teachers** must own the resource
- **Students** must be enrolled in the course/class

---

## Testing Checklist

1. **Test Course Access:**
   - Login as student
   - Try to access a course you're enrolled in → Should work ✅
   - Try to access a course you're NOT enrolled in → Should return 403 ❌

2. **Test Class Access:**
   - Login as student
   - Try to access a class you're enrolled in → Should work ✅
   - Try to access a class you're NOT enrolled in → Should return 403 ❌

3. **Test Assignment Access:**
   - Login as student
   - Click on an assignment from a class you're enrolled in → Should work ✅
   - Try to access an assignment from a class you're NOT enrolled in → Should return 403 ❌

---

## Required Imports

Make sure you have these imports in your controllers:

```java
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
```






