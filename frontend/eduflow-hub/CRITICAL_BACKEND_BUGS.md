# 🔴 Critical Backend Bugs Found

## Bug 1: getAssignmentsByClass Uses Wrong ID

**Location:** `AssignmentController.getAssignmentsByClass()`

**Problem:**
```java
@GetMapping("/class/{classId}")
public ResponseEntity<?> getAssignmentsByClass(
        @PathVariable Long classId,  // ← Receives classId
        Authentication authentication) {
    // ...
    Optional<Course> courseOptional = courseService.getCourseById(classId);  // ❌ BUG: Using classId as courseId!
    // ...
    List<AssignmentDTO> assignments = assignmentService.getAssignmentsByClass(course.getId());
}
```

**Fix:**
```java
@GetMapping("/class/{classId}")
@PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
public ResponseEntity<?> getAssignmentsByClass(
        @PathVariable Long classId,
        Authentication authentication) {
    try {
        // ✅ FIX: Get class first, then extract courseId
        ClassEntity classEntity = classService.getClassById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));
        
        Long courseId = classEntity.getCourseId();
        
        Optional<Course> courseOptional = courseService.getCourseById(courseId);
        if (courseOptional.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Course not found for class id: " + classId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        List<AssignmentDTO> assignments = assignmentService.getAssignmentsByClass(courseId);
        return ResponseEntity.ok(assignments);

    } catch (Exception e) {
        Map<String, String> error = new HashMap<>();
        error.put("message", "Failed to fetch assignments: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

**Why this matters:** The endpoint is supposed to get assignments for a class, but it's treating the classId as a courseId, which will return wrong results or fail.

---

## Bug 2: Assignment Entity Has Duplicate Constructors

**Location:** `Assignment.java`

**Problem:**
Your Assignment entity has:
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Assignment {
    // ...
    
    public Assignment() {  // ❌ Duplicate! Lombok already creates this
        super();
        // TODO Auto-generated constructor stub
    }

    public Assignment(Long id, Long courseId, String title, String description, 
                     LocalDateTime dueDate, Double maxGrade) {  // ❌ Duplicate! Lombok already creates this
        // ...
    }
}
```

**Fix:**
Remove the explicit constructors and let Lombok handle them, OR remove Lombok annotations and keep explicit constructors:

**Option 1: Use Lombok (Recommended)**
```java
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Assignment {
    // ... fields only, no explicit constructors
}
```

**Option 2: Remove Lombok Annotations**
```java
@Entity
public class Assignment {
    // ... fields
    
    // Explicit constructors
    public Assignment() {
        this.allowLateSubmission = false;
        this.status = "published";
    }
    
    // ... other constructors and getters/setters
}
```

**Why this matters:** Having both Lombok annotations and explicit constructors can cause compilation errors or unexpected behavior.

---

## Bug 3: Missing ClassService Dependency

**Location:** `AssignmentController.getAssignmentsByClass()`

**Problem:**
The fix for Bug 1 requires `classService.getClassById()`, but `ClassService` might not be injected.

**Fix:**
Add `ClassService` dependency:

```java
@RestController
@RequestMapping("/assignments")
public class AssignmentController {
    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserService userService;
    
    // ✅ ADD THIS:
    @Autowired
    private ClassService classService;  // Or whatever your class service is called
}
```

**Note:** If your class service has a different name (e.g., `ClassEntityService`), use that instead.

---

## Summary

1. **Bug 1** - CRITICAL: Fix `getAssignmentsByClass()` to resolve classId → courseId
2. **Bug 2** - HIGH: Remove duplicate constructors in Assignment entity
3. **Bug 3** - HIGH: Add ClassService dependency to AssignmentController

Fix these before implementing the new assignment features!

