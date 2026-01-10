# 🔧 Fix for Assignment JSON Response Error

## Problem

When clicking on an assignment, the frontend gets:
- **Error**: `failed to execute json on responses unexpected end of json`
- **Backend Log**: `Using 'application/octet-stream', given [*/*] and supported [*/*]`

The backend is returning `application/octet-stream` instead of `application/json`, which means the response can't be parsed as JSON.

## Root Cause

The `AssignmentController.getAssignmentById()` method is likely:
1. Returning the `Assignment` entity directly instead of `AssignmentDTO`
2. Not properly converting the entity to DTO
3. Missing proper `ResponseEntity` with content type
4. Throwing an exception that's not being caught

## Solution

Update `AssignmentController.getAssignmentById()` to ensure it returns proper JSON:

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
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(error);
        }

        Assignment assignment = assignmentOptional.get();
        
        // Get the course for this assignment
        Optional<Course> courseOptional = courseService.getCourseById(assignment.getCourseId());
        if (courseOptional.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Course not found for assignment");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(error);
        }

        Course course = courseOptional.get();

        // ✅ Authorization check:
        // - Teachers must own the course
        // - Students must be enrolled in the course (directly OR via a class)
        if (user.getRole().equals("TEACHER")) {
            if (!course.getTeacherId().equals(user.getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Unauthorized: Assignment does not belong to this teacher");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(error);
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
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(error);
            }
        }

        // ✅ CRITICAL: Convert entity to DTO before returning
        AssignmentDTO assignmentDTO = assignmentService.convertToAssignmentDTO(assignment);
        
        // ✅ Explicitly set content type to JSON
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(assignmentDTO);

    } catch (RuntimeException e) {
        Map<String, String> error = new HashMap<>();
        error.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
    } catch (Exception e) {
        Map<String, String> error = new HashMap<>();
        error.put("message", "Failed to fetch assignment: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
    }
}
```

## Required Imports

Add these imports to `AssignmentController`:

```java
import org.springframework.http.MediaType;
import com.elearnhub.teacher_service.service.ClassService;
import com.elearnhub.teacher_service.dto.ClassDTO;
import java.util.List;
```

## Required Dependencies

Make sure `AssignmentController` has:

```java
@Autowired
private ClassService classService;  // ✅ ADD THIS if not already present
```

## Alternative: Use AssignmentService Method

If your `AssignmentService` has a `getAssignmentById()` method that returns `AssignmentDTO`, use it:

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

        // ✅ Use service method to get DTO directly
        AssignmentDTO assignmentDTO = assignmentService.getAssignmentById(assignmentId);
        
        if (assignmentDTO == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Assignment not found with ID: " + assignmentId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(error);
        }

        // Get the course for authorization check
        Optional<Course> courseOptional = courseService.getCourseById(assignmentDTO.getCourseId());
        if (courseOptional.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Course not found for assignment");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(error);
        }

        Course course = courseOptional.get();

        // ✅ Authorization check (same as above)
        if (user.getRole().equals("TEACHER")) {
            if (!course.getTeacherId().equals(user.getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Unauthorized: Assignment does not belong to this teacher");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(error);
            }
        } else if (user.getRole().equals("STUDENT")) {
            boolean isEnrolled = false;
            
            if (course.getStudents() != null) {
                course.getStudents().size();
                isEnrolled = course.getStudents().stream()
                    .anyMatch(student -> student.getId().equals(user.getId()));
            }
            
            if (!isEnrolled) {
                List<ClassDTO> studentClasses = classService.getClassesByStudent(user.getId());
                isEnrolled = studentClasses.stream()
                    .anyMatch(classDTO -> classDTO.getCourseId() != null && 
                                         classDTO.getCourseId().equals(course.getId()));
            }
            
            if (!isEnrolled) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Unauthorized: You are not enrolled in this course");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(error);
            }
        }

        // ✅ Return DTO with explicit JSON content type
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(assignmentDTO);

    } catch (RuntimeException e) {
        Map<String, String> error = new HashMap<>();
        error.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
    } catch (Exception e) {
        Map<String, String> error = new HashMap<>();
        error.put("message", "Failed to fetch assignment: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
    }
}
```

## Key Points

1. **Always return `AssignmentDTO`**, never the `Assignment` entity directly
2. **Explicitly set content type** to `MediaType.APPLICATION_JSON`
3. **Use `@Transactional`** to prevent lazy loading issues
4. **Check both direct and indirect enrollment** for students
5. **Handle all exceptions** and return proper JSON error responses

## Verify AssignmentService.convertToAssignmentDTO()

Make sure your `AssignmentService` has a method to convert entity to DTO:

```java
public AssignmentDTO convertToAssignmentDTO(Assignment assignment) {
    AssignmentDTO dto = new AssignmentDTO();
    dto.setId(assignment.getId());
    dto.setTitle(assignment.getTitle());
    dto.setDescription(assignment.getDescription());
    dto.setCourseId(assignment.getCourseId());
    dto.setDueDate(assignment.getDueDate());
    dto.setMaxGrade(assignment.getMaxGrade());
    dto.setWeight(assignment.getWeight());
    dto.setAllowLateSubmission(assignment.isAllowLateSubmission());
    dto.setLatePenalty(assignment.getLatePenalty());
    dto.setAdditionalInstructions(assignment.getAdditionalInstructions());
    dto.setStatus(assignment.getStatus());
    return dto;
}
```

## Testing

After implementing the fix:

1. ✅ The response should have `Content-Type: application/json`
2. ✅ The frontend should be able to parse the JSON response
3. ✅ Students enrolled in classes should be able to access assignments
4. ✅ Teachers should be able to access their own assignments






