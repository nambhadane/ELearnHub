# Backend Changes Required for Create Assignment Feature

## Overview
The frontend Create Assignment form has several fields that are not currently supported by the backend. This document outlines all required changes to make the assignment creation feature fully functional.

---

## 🔴 Critical Changes Required

### 1. Update AssignmentDTO

**Current AssignmentDTO** only has:
- `id`, `title`, `description`, `dueDate`, `maxGrade`, `courseId`

**Required Fields** (based on frontend):
- ✅ `title` - Already exists
- ✅ `description` - Already exists
- ✅ `dueDate` - Already exists (combine date + time from frontend)
- ✅ `maxGrade` - Already exists (Total Points)
- ✅ `courseId` - Already exists (but frontend selects "class", see note below)
- ❌ `weight` - NEW: Percentage weight of assignment (optional)
- ❌ `allowLateSubmission` - NEW: Boolean flag
- ❌ `latePenalty` - NEW: Percentage penalty for late submissions (optional, only if allowLateSubmission = true)
- ❌ `additionalInstructions` - NEW: Additional notes (optional, or merge with description)
- ❌ `status` - NEW: "draft" or "published" (for Save as Draft feature)

**Updated AssignmentDTO:**

```java
package com.elearnhub.teacher_service.dto;

import java.time.LocalDateTime;

public class AssignmentDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private Double maxGrade;
    private Long courseId;
    
    // NEW FIELDS
    private Double weight;                    // Optional: Weight percentage (e.g., 20.0 for 20%)
    private Boolean allowLateSubmission;      // Default: false
    private Double latePenalty;               // Optional: Penalty percentage (e.g., 10.0 for 10%)
    private String additionalInstructions;   // Optional: Additional notes
    private String status;                    // "draft" or "published" (default: "published")

    // Constructors
    public AssignmentDTO() {
        this.allowLateSubmission = false;
        this.status = "published";
    }

    public AssignmentDTO(Long id, String title, String description, LocalDateTime dueDate, 
                         Double maxGrade, Long courseId) {
        this();
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.maxGrade = maxGrade;
        this.courseId = courseId;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

    public Double getMaxGrade() { return maxGrade; }
    public void setMaxGrade(Double maxGrade) { this.maxGrade = maxGrade; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    // NEW GETTERS AND SETTERS
    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }

    public Boolean getAllowLateSubmission() { return allowLateSubmission; }
    public void setAllowLateSubmission(Boolean allowLateSubmission) { 
        this.allowLateSubmission = allowLateSubmission; 
    }

    public Double getLatePenalty() { return latePenalty; }
    public void setLatePenalty(Double latePenalty) { this.latePenalty = latePenalty; }

    public String getAdditionalInstructions() { return additionalInstructions; }
    public void setAdditionalInstructions(String additionalInstructions) { 
        this.additionalInstructions = additionalInstructions; 
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
```

---

### 2. Update Assignment Entity

**Add new fields to Assignment entity:**

```java
package com.elearnhub.teacher_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Assignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Title cannot be null")
    @Size(min = 2, max = 100, message = "Title must be between 2 and 100 characters")
    private String title;

    @NotNull(message = "Description cannot be null")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotNull(message = "Due date cannot be null")
    private LocalDateTime dueDate;

    @NotNull(message = "Max grade cannot be null")
    private Double maxGrade;

    @NotNull(message = "Course ID cannot be null")
    private Long courseId;

    // NEW FIELDS
    @Column(name = "weight")
    private Double weight;  // Optional: Weight percentage

    @Column(name = "allow_late_submission", nullable = false)
    private Boolean allowLateSubmission = false;

    @Column(name = "late_penalty")
    private Double latePenalty;  // Optional: Penalty percentage

    @Column(name = "additional_instructions", length = 1000)
    private String additionalInstructions;  // Optional

    @Column(name = "status", nullable = false, length = 20)
    private String status = "published";  // "draft" or "published"

    // Constructors
    public Assignment() {
        this.allowLateSubmission = false;
        this.status = "published";
    }

    // Getters and Setters (Lombok @Data should generate these, but you can add explicit ones if needed)
}
```

**Database Migration:**
You'll need to add these columns to your `assignment` table:

```sql
ALTER TABLE assignment 
ADD COLUMN weight DOUBLE,
ADD COLUMN allow_late_submission BOOLEAN DEFAULT FALSE,
ADD COLUMN late_penalty DOUBLE,
ADD COLUMN additional_instructions VARCHAR(1000),
ADD COLUMN status VARCHAR(20) DEFAULT 'published';
```

---

### 3. Update AssignmentController - Create Assignment Endpoint

**Current endpoint accepts `courseId`, but frontend sends `classId`.**

**Option A: Accept `classId` and resolve to `courseId` (Recommended)**

```java
@PostMapping
@PreAuthorize("hasRole('TEACHER')")
public ResponseEntity<?> createAssignment(
        @RequestBody AssignmentDTO assignmentDTO,
        Authentication authentication) {
    try {
        String username = authentication.getName();
        User teacher = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        // ✅ NEW: If classId is provided instead of courseId, resolve it
        Long courseId = assignmentDTO.getCourseId();
        if (assignmentDTO.getClassId() != null) {
            // Get class and extract courseId
            ClassEntity classEntity = classService.getClassById(assignmentDTO.getClassId())
                    .orElseThrow(() -> new RuntimeException("Class not found"));
            
            // Verify teacher owns this class
            if (!classEntity.getTeacherId().equals(teacher.getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Unauthorized: Class does not belong to this teacher");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            courseId = classEntity.getCourseId();
            assignmentDTO.setCourseId(courseId);
        }

        // Validate course exists and belongs to teacher
        Optional<Course> courseOptional = courseService.getCourseById(courseId);
        if (courseOptional.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Course not found with id: " + courseId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        Course course = courseOptional.get();
        if (!course.getTeacherId().equals(teacher.getId())) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Unauthorized: Course does not belong to this teacher");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        // Set defaults for new fields
        if (assignmentDTO.getAllowLateSubmission() == null) {
            assignmentDTO.setAllowLateSubmission(false);
        }
        if (assignmentDTO.getStatus() == null || assignmentDTO.getStatus().isEmpty()) {
            assignmentDTO.setStatus("published");
        }

        AssignmentDTO createdAssignment = assignmentService.createAssignment(assignmentDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAssignment);

    } catch (Exception e) {
        Map<String, String> error = new HashMap<>();
        error.put("message", "Failed to create assignment: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

**Option B: Frontend resolves classId to courseId (Simpler, no backend change)**

Frontend will:
1. Get selected class
2. Extract `courseId` from class
3. Send `courseId` to backend

**Recommendation:** Use Option B (frontend resolves) for now, as it requires no backend changes.

---

### 4. Update AssignmentService

**Ensure AssignmentService.createAssignment() handles new fields:**

```java
public AssignmentDTO createAssignment(AssignmentDTO assignmentDTO) {
    Assignment assignment = new Assignment();
    assignment.setTitle(assignmentDTO.getTitle());
    assignment.setDescription(assignmentDTO.getDescription());
    assignment.setDueDate(assignmentDTO.getDueDate());
    assignment.setMaxGrade(assignmentDTO.getMaxGrade());
    assignment.setCourseId(assignmentDTO.getCourseId());
    
    // NEW: Set new fields
    assignment.setWeight(assignmentDTO.getWeight());
    assignment.setAllowLateSubmission(
        assignmentDTO.getAllowLateSubmission() != null 
            ? assignmentDTO.getAllowLateSubmission() 
            : false
    );
    assignment.setLatePenalty(assignmentDTO.getLatePenalty());
    assignment.setAdditionalInstructions(assignmentDTO.getAdditionalInstructions());
    assignment.setStatus(
        assignmentDTO.getStatus() != null && !assignmentDTO.getStatus().isEmpty()
            ? assignmentDTO.getStatus()
            : "published"
    );

    Assignment saved = assignmentRepository.save(assignment);
    return convertToDTO(saved);
}
```

**Update convertToDTO() method:**

```java
private AssignmentDTO convertToDTO(Assignment assignment) {
    AssignmentDTO dto = new AssignmentDTO();
    dto.setId(assignment.getId());
    dto.setTitle(assignment.getTitle());
    dto.setDescription(assignment.getDescription());
    dto.setDueDate(assignment.getDueDate());
    dto.setMaxGrade(assignment.getMaxGrade());
    dto.setCourseId(assignment.getCourseId());
    
    // NEW: Map new fields
    dto.setWeight(assignment.getWeight());
    dto.setAllowLateSubmission(assignment.getAllowLateSubmission());
    dto.setLatePenalty(assignment.getLatePenalty());
    dto.setAdditionalInstructions(assignment.getAdditionalInstructions());
    dto.setStatus(assignment.getStatus());
    
    return dto;
}
```

---

## 🟡 Optional Enhancements

### 5. Get Assignments by Class (Not Course)

**Current:** `GET /assignments/class/{classId}` - but it uses classId as courseId (BUG!)

**Fix:** Resolve classId to courseId first:

```java
@GetMapping("/class/{classId}")
@PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
public ResponseEntity<?> getAssignmentsByClass(
        @PathVariable Long classId,
        Authentication authentication) {
    try {
        // ✅ FIX: Get class first, then get courseId
        ClassEntity classEntity = classService.getClassById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));
        
        Long courseId = classEntity.getCourseId();
        
        // Validate course exists
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

---

### 6. Add Get Assignment by ID Endpoint

**For viewing assignment details:**

```java
@GetMapping("/{assignmentId}")
@PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
public ResponseEntity<?> getAssignmentById(
        @PathVariable Long assignmentId,
        Authentication authentication) {
    try {
        AssignmentDTO assignment = assignmentService.getAssignmentById(assignmentId);
        return ResponseEntity.ok(assignment);
    } catch (RuntimeException e) {
        Map<String, String> error = new HashMap<>();
        error.put("message", "Assignment not found with id: " + assignmentId);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}
```

---

### 7. Update Assignment Endpoint

**For editing assignments:**

```java
@PutMapping("/{assignmentId}")
@PreAuthorize("hasRole('TEACHER')")
public ResponseEntity<?> updateAssignment(
        @PathVariable Long assignmentId,
        @RequestBody AssignmentDTO assignmentDTO,
        Authentication authentication) {
    try {
        String username = authentication.getName();
        User teacher = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        // Verify assignment exists and belongs to teacher
        AssignmentDTO existing = assignmentService.getAssignmentById(assignmentId);
        Optional<Course> courseOptional = courseService.getCourseById(existing.getCourseId());
        
        if (courseOptional.isEmpty() || 
            !courseOptional.get().getTeacherId().equals(teacher.getId())) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Unauthorized: Assignment does not belong to this teacher");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        assignmentDTO.setId(assignmentId);
        AssignmentDTO updated = assignmentService.updateAssignment(assignmentDTO);
        return ResponseEntity.ok(updated);

    } catch (RuntimeException e) {
        Map<String, String> error = new HashMap<>();
        error.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    } catch (Exception e) {
        Map<String, String> error = new HashMap<>();
        error.put("message", "Failed to update assignment: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

---

## 📋 Summary of Changes

### Required:
1. ✅ Add new fields to `AssignmentDTO`: `weight`, `allowLateSubmission`, `latePenalty`, `additionalInstructions`, `status`
2. ✅ Add new columns to `Assignment` entity
3. ✅ Update `AssignmentService.createAssignment()` to handle new fields
4. ✅ Update `AssignmentService.convertToDTO()` to map new fields
5. ✅ Fix `GET /assignments/class/{classId}` to resolve classId → courseId

### Optional but Recommended:
6. ✅ Add `GET /assignments/{assignmentId}` endpoint
7. ✅ Add `PUT /assignments/{assignmentId}` endpoint for updates

### Database:
- Run SQL migration to add new columns to `assignment` table

---

## 🧪 Testing Checklist

After implementing changes:

1. ✅ Create assignment with all fields
2. ✅ Create assignment with "Save as Draft" (status = "draft")
3. ✅ Create assignment with late submission enabled
4. ✅ Get assignments by class ID
5. ✅ Get assignment by ID
6. ✅ Update assignment
7. ✅ Delete assignment
8. ✅ Verify validation (required fields, date formats, etc.)

---

## 📝 Notes

- **Date/Time Handling**: Frontend sends separate `dueDate` (date) and `dueTime` (time). Backend expects `LocalDateTime`. Frontend will combine them before sending.
- **Class vs Course**: Frontend selects "class", but backend uses `courseId`. Frontend will extract `courseId` from selected class before sending to backend.
- **Default Values**: 
  - `allowLateSubmission` = `false`
  - `status` = `"published"`
  - `weight`, `latePenalty`, `additionalInstructions` = `null` (optional)

---

## 🚀 Implementation Priority

1. **High Priority** (Required for basic functionality):
   - Update AssignmentDTO and Entity
   - Update AssignmentService
   - Database migration

2. **Medium Priority** (Better UX):
   - Fix GET assignments by class endpoint
   - Add GET assignment by ID

3. **Low Priority** (Nice to have):
   - Add UPDATE assignment endpoint
   - Add validation for weight/latePenalty ranges

