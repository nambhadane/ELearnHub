# Corrected Backend Code - Ready to Paste

## 1. AssignmentController.java (CORRECTED)

```java
package com.elearnhub.teacher_service.Controller;

import com.elearnhub.teacher_service.dto.AssignmentDTO;
import com.elearnhub.teacher_service.dto.SubmissionDTO;
import com.elearnhub.teacher_service.entity.Assignment;
import com.elearnhub.teacher_service.entity.ClassEntity;
import com.elearnhub.teacher_service.entity.Course;
import com.elearnhub.teacher_service.entity.User;
import com.elearnhub.teacher_service.service.AssignmentService;
import com.elearnhub.teacher_service.service.ClassService;
import com.elearnhub.teacher_service.service.CourseService;
import com.elearnhub.teacher_service.service.UserService;
import io.jsonwebtoken.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/assignments")
public class AssignmentController {

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserService userService;

    @Autowired
    private ClassService classService;

    // Inner class for grade request
    static class GradeRequest {
        private Double grade;
        private String feedback;

        public Double getGrade() { return grade; }
        public void setGrade(Double grade) { this.grade = grade; }
        public String getFeedback() { return feedback; }
        public void setFeedback(String feedback) { this.feedback = feedback; }
    }

    // Create assignment - validates Course exists and belongs to teacher
    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> createAssignment(
            @RequestBody AssignmentDTO assignmentDTO,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            User teacher = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Teacher not found"));

            // ✅ FIXED: If classId is provided instead of courseId, resolve it
            Long courseId = assignmentDTO.getCourseId();

            if (assignmentDTO.getClassId() != null) {
                // Get class and extract courseId
                ClassEntity classEntity = classService.getClassById(assignmentDTO.getClassId())
                        .orElseThrow(() -> new RuntimeException("Class not found"));

                // ✅ FIXED: Check teacher ownership correctly
                // Option 1: If ClassEntity has getTeacher() returning User object
                if (classEntity.getTeacher() != null && !classEntity.getTeacher().getId().equals(teacher.getId())) {
                    Map<String, String> error = new HashMap<>();
                    error.put("message", "Unauthorized: Class does not belong to this teacher");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                }
                // Option 2: If ClassEntity has getTeacherId() returning Long
                // if (classEntity.getTeacherId() != null && !classEntity.getTeacherId().equals(teacher.getId())) {
                //     Map<String, String> error = new HashMap<>();
                //     error.put("message", "Unauthorized: Class does not belong to this teacher");
                //     return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                // }

                // ✅ FIXED: Get courseId from class
                // Option 1: If ClassEntity has getCourse() returning Course object
                if (classEntity.getCourse() != null) {
                    courseId = classEntity.getCourse().getId();
                }
                // Option 2: If ClassEntity has getCourseId() returning Long
                // else if (classEntity.getCourseId() != null) {
                //     courseId = classEntity.getCourseId();
                // }
                else {
                    Map<String, String> error = new HashMap<>();
                    error.put("message", "Class does not have an associated course");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
                }

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

    // ✅ FIXED: Get assignments by class - resolves classId to courseId
    @GetMapping("/class/{classId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<?> getAssignmentsByClass(
            @PathVariable Long classId,
            Authentication authentication) {
        try {
            // ✅ FIXED: Get class first, then extract courseId
            ClassEntity classEntity = classService.getClassById(classId)
                    .orElseThrow(() -> new RuntimeException("Class not found"));

            Long courseId;
            // Option 1: If ClassEntity has getCourse() returning Course object
            if (classEntity.getCourse() != null) {
                courseId = classEntity.getCourse().getId();
            }
            // Option 2: If ClassEntity has getCourseId() returning Long
            // else if (classEntity.getCourseId() != null) {
            //     courseId = classEntity.getCourseId();
            // }
            else {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Class does not have an associated course");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

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

    // Get submissions by assignment - validates Assignment exists and belongs to teacher
    @GetMapping("/{assignmentId}/submissions")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> getSubmissionsByAssignment(
            @PathVariable Long assignmentId,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            User teacher = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Teacher not found"));

            AssignmentDTO assignmentDTO;
            try {
                assignmentDTO = assignmentService.getAssignmentById(assignmentId);
            } catch (RuntimeException e) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Assignment not found with id: " + assignmentId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            Optional<Course> courseOptional = courseService.getCourseById(assignmentDTO.getCourseId());
            if (courseOptional.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Course not found for this assignment");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            Course course = courseOptional.get();
            if (!course.getTeacherId().equals(teacher.getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Unauthorized: Assignment does not belong to this teacher");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            List<SubmissionDTO> submissions = assignmentService.getSubmissionsByAssignment(assignmentId);
            return ResponseEntity.ok(submissions);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to fetch submissions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get the authenticated student's submission for a specific assignment
    @GetMapping("/{assignmentId}/submission/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getMySubmission(
            @PathVariable Long assignmentId,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            User student = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            SubmissionDTO submissionDTO = assignmentService.getSubmissionByStudentAndAssignment(
                    student.getId(), assignmentId);

            if (submissionDTO == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            return ResponseEntity.ok(submissionDTO);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to fetch submission: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping(value = "/submissions", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> saveSubmission(
            @RequestPart(required = false) SubmissionDTO submissionDTO,
            @RequestPart(required = false) List<MultipartFile> files,
            @RequestParam(required = false) Long assignmentId,
            @RequestParam(required = false) String content,
            Authentication authentication) {
        try {
            // Get authenticated student
            String username = authentication.getName();
            User student = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            // Handle both JSON and form-data requests
            SubmissionDTO finalSubmissionDTO;
            if (submissionDTO != null) {
                // JSON request
                finalSubmissionDTO = submissionDTO;
            } else {
                // Form-data request
                finalSubmissionDTO = new SubmissionDTO();
                finalSubmissionDTO.setAssignmentId(assignmentId);
                finalSubmissionDTO.setContent(content);
            }

            // ✅ CRITICAL FIX: Set studentId from authenticated user (not from request)
            finalSubmissionDTO.setStudentId(student.getId());

            // Validate assignmentId is provided
            if (finalSubmissionDTO.getAssignmentId() == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Assignment ID is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            // Handle file uploads if provided
            if (files != null && !files.isEmpty()) {
                try {
                    // Save files and get file paths
                    List<String> filePaths = new ArrayList<>();
                    for (MultipartFile file : files) {
                        if (!file.isEmpty()) {
                            // Generate unique filename
                            String originalFilename = file.getOriginalFilename();
                            String timestamp = String.valueOf(System.currentTimeMillis());
                            String filename = timestamp + "_" + originalFilename;

                            // Save file (adjust path as needed)
                            String uploadDir = "uploads/submissions/";
                            File uploadDirectory = new File(uploadDir);
                            if (!uploadDirectory.exists()) {
                                uploadDirectory.mkdirs();
                            }

                            String filePath = uploadDir + filename;
                            File destFile = new File(filePath);
                            file.transferTo(destFile);

                            filePaths.add(filePath);
                        }
                    }

                    // Store multiple file paths as comma-separated
                    finalSubmissionDTO.setFilePath(String.join(",", filePaths));

                } catch (IOException e) {
                    Map<String, String> error = new HashMap<>();
                    error.put("message", "Failed to save file: " + e.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
                }
            }

            SubmissionDTO savedSubmission = assignmentService.saveSubmission(finalSubmissionDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedSubmission);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to save submission: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Grade submission (for teachers)
    @PutMapping("/submissions/{submissionId}/grade")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> gradeSubmission(
            @PathVariable Long submissionId,
            @RequestBody GradeRequest gradeRequest,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            User teacher = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Teacher not found"));

            if (gradeRequest.getGrade() == null || gradeRequest.getGrade() < 0 || gradeRequest.getGrade() > 100) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Grade must be between 0 and 100");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            SubmissionDTO submissionDTO = assignmentService.gradeSubmission(
                    submissionId,
                    gradeRequest.getGrade(),
                    gradeRequest.getFeedback()
            );

            AssignmentDTO assignmentDTO = assignmentService.getAssignmentById(submissionDTO.getAssignmentId());
            Optional<Course> courseOptional = courseService.getCourseById(assignmentDTO.getCourseId());

            if (courseOptional.isEmpty() || !courseOptional.get().getTeacherId().equals(teacher.getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Unauthorized: Cannot grade this submission");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            return ResponseEntity.ok(submissionDTO);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to grade submission: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Delete assignment - validates Assignment exists and belongs to teacher's course
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> deleteAssignment(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            User teacher = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Teacher not found"));

            AssignmentDTO assignmentDTO;
            try {
                assignmentDTO = assignmentService.getAssignmentById(id);
            } catch (RuntimeException e) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Assignment not found with id: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            Optional<Course> courseOptional = courseService.getCourseById(assignmentDTO.getCourseId());
            if (courseOptional.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Course not found for this assignment");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            Course course = courseOptional.get();
            if (!course.getTeacherId().equals(teacher.getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Unauthorized: Assignment does not belong to this teacher");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            assignmentService.deleteAssignment(id);
            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to delete assignment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get assignments for enrolled classes (for students)
    @GetMapping("/my-assignments")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getMyAssignments(Authentication authentication) {
        try {
            String username = authentication.getName();
            User student = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            List<Course> enrolledCourses = courseService.getCoursesByStudentId(student.getId());
            if (enrolledCourses == null || enrolledCourses.isEmpty()) {
                return ResponseEntity.ok(List.of());
            }

            List<Map<String, Object>> allAssignments = new ArrayList<>();
            for (Course course : enrolledCourses) {
                if (course.getId() != null) {
                    List<AssignmentDTO> assignments = assignmentService.getAssignmentsByClass(course.getId());
                    for (AssignmentDTO assignment : assignments) {
                        Map<String, Object> assignmentData = new HashMap<>();
                        assignmentData.put("id", assignment.getId());
                        assignmentData.put("title", assignment.getTitle());
                        assignmentData.put("description", assignment.getDescription());
                        assignmentData.put("dueDate", assignment.getDueDate());
                        assignmentData.put("maxGrade", assignment.getMaxGrade());
                        assignmentData.put("courseId", assignment.getCourseId());
                        assignmentData.put("className", course.getName());

                        SubmissionDTO submission = assignmentService.getSubmissionByStudentAndAssignment(
                                student.getId(), assignment.getId());

                        if (submission != null && submission.getId() != null) {
                            assignmentData.put("status", submission.getGrade() != null ? "graded" : "submitted");
                            assignmentData.put("submissionId", submission.getId());
                            assignmentData.put("submittedAt", submission.getSubmittedAt());
                            assignmentData.put("grade", submission.getGrade());
                            assignmentData.put("feedback", submission.getFeedback());
                        } else {
                            assignmentData.put("status", "pending");
                            assignmentData.put("submissionId", null);
                            assignmentData.put("submittedAt", null);
                            assignmentData.put("grade", null);
                            assignmentData.put("feedback", null);
                        }

                        allAssignments.add(assignmentData);
                    }
                }
            }

            allAssignments.sort((a, b) -> {
                LocalDateTime dueDateA = (LocalDateTime) a.get("dueDate");
                LocalDateTime dueDateB = (LocalDateTime) b.get("dueDate");
                if (dueDateA == null || dueDateB == null) return 0;
                return dueDateA.compareTo(dueDateB);
            });

            return ResponseEntity.ok(allAssignments);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to fetch assignments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
```

---

## 2. AssignmentDTO.java (CORRECTED)

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
    private Long classId;                    // ✅ ADDED: For frontend to send classId
    private Double weight;                   // Optional: Weight percentage (e.g., 20.0 for 20%)
    private Boolean allowLateSubmission;     // Default: false
    private Double latePenalty;              // Optional: Penalty percentage (e.g., 10.0 for 10%)
    private String additionalInstructions;   // Optional: Additional notes
    private String status;                   // "draft" or "published" (default: "published")

    // ✅ FIXED: Default constructor with proper defaults
    public AssignmentDTO() {
        this.allowLateSubmission = false;
        this.status = "published";
    }

    // Constructor with basic fields
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

    // Full constructor (optional, for convenience)
    public AssignmentDTO(Long id, String title, String description, LocalDateTime dueDate, 
                         Double maxGrade, Long courseId, Double weight, Boolean allowLateSubmission, 
                         Double latePenalty, String additionalInstructions, String status) {
        this();
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.maxGrade = maxGrade;
        this.courseId = courseId;
        this.weight = weight;
        this.allowLateSubmission = allowLateSubmission;
        this.latePenalty = latePenalty;
        this.additionalInstructions = additionalInstructions;
        this.status = status;
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

    // ✅ ADDED: classId getter and setter
    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }

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

## 3. AssignmentService.java (CORRECTED - Already Good, Just Minor Fix)

```java
package com.elearnhub.teacher_service.service;

import com.elearnhub.teacher_service.dto.AssignmentDTO;
import com.elearnhub.teacher_service.dto.SubmissionDTO;
import com.elearnhub.teacher_service.entity.Assignment;
import com.elearnhub.teacher_service.entity.Submission;
import com.elearnhub.teacher_service.entity.User;
import com.elearnhub.teacher_service.repository.AssignmentRepository;
import com.elearnhub.teacher_service.repository.SubmissionRepository;
import com.elearnhub.teacher_service.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AssignmentService {

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private UserService userService;

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

    public List<AssignmentDTO> getAssignmentsByClass(Long courseId) {
        List<Assignment> assignments = assignmentRepository.findByCourseId(courseId);
        return assignments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public AssignmentDTO getAssignmentById(Long id) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found with id: " + id));
        return convertToDTO(assignment);
    }

    public void deleteAssignment(Long id) {
        if (!assignmentRepository.existsById(id)) {
            throw new RuntimeException("Assignment not found with id: " + id);
        }
        assignmentRepository.deleteById(id);
    }

    public SubmissionDTO saveSubmission(SubmissionDTO submissionDTO) {
        Assignment assignment = assignmentRepository.findById(submissionDTO.getAssignmentId())
                .orElseThrow(() -> new RuntimeException("Assignment not found with id: " + submissionDTO.getAssignmentId()));

        User student = userService.getUserById(submissionDTO.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + submissionDTO.getStudentId()));

        Submission submission = submissionRepository
                .findByAssignmentIdAndStudentId(assignment.getId(), student.getId())
                .orElse(new Submission());

        submission.setAssignment(assignment);
        submission.setStudent(student);
        submission.setContent(submissionDTO.getContent());

        // Only overwrite file paths when the student uploads new files
        if (submissionDTO.getFilePath() != null && !submissionDTO.getFilePath().isEmpty()) {
            submission.setFilePath(submissionDTO.getFilePath());
        }

        submission.setSubmittedAt(LocalDateTime.now());

        // Reset grading metadata on resubmission so the teacher re-evaluates the work
        if (submission.getId() != null) {
            submission.setGrade(null);
            submission.setFeedback(null);
        }

        Submission savedSubmission = submissionRepository.save(submission);
        return convertSubmissionToDTO(savedSubmission);
    }

    public List<SubmissionDTO> getSubmissionsByAssignment(Long assignmentId) {
        List<Submission> submissions = submissionRepository.findByAssignmentId(assignmentId);
        return submissions.stream()
                .map(this::convertSubmissionToDTO)
                .collect(Collectors.toList());
    }

    public SubmissionDTO gradeSubmission(Long submissionId, Double score, String feedback) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found with id: " + submissionId));

        submission.setGrade(score);
        submission.setFeedback(feedback);

        submissionRepository.save(submission);
        return convertSubmissionToDTO(submission);
    }

    private SubmissionDTO convertSubmissionToDTO(Submission submission) {
        SubmissionDTO dto = new SubmissionDTO();
        dto.setId(submission.getId());

        if (submission.getAssignment() != null) {
            dto.setAssignmentId(submission.getAssignment().getId());
        }

        if (submission.getStudent() != null) {
            dto.setStudentId(submission.getStudent().getId());

            String studentName = submission.getStudent().getName() != null && 
                    !submission.getStudent().getName().trim().isEmpty()
                    ? submission.getStudent().getName()
                    : submission.getStudent().getUsername();
            dto.setStudentName(studentName);
        }

        dto.setContent(submission.getContent());
        dto.setFilePath(submission.getFilePath());
        dto.setSubmittedAt(submission.getSubmittedAt());
        dto.setGrade(submission.getGrade());
        dto.setFeedback(submission.getFeedback());

        return dto;
    }

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

    public SubmissionDTO getSubmissionByStudentAndAssignment(Long studentId, Long assignmentId) {
        Optional<Submission> submissionOpt = submissionRepository.findByAssignmentIdAndStudentId(
                assignmentId, studentId);
        if (submissionOpt.isEmpty()) {
            return null;
        }
        return convertSubmissionToDTO(submissionOpt.get());
    }
}
```

---

## 4. ClassService.java (ADD MISSING METHOD)

**Problem:** `AssignmentController` calls `classService.getClassById()` but this method doesn't exist.

**Solution:** Add this method to your `ClassService.java`:

```java
// ✅ ADD THIS METHOD to ClassService
public Optional<ClassEntity> getClassById(Long classId) {
    return classEntityRepository.findById(classId);
}
```

**Full Updated ClassService.java:**

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
import java.util.Optional;  // ✅ ADD THIS IMPORT
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

---

## ⚠️ IMPORTANT NOTES:

### For AssignmentController.java:

**You need to check your ClassEntity structure and uncomment the correct option:**

1. **If ClassEntity has:**
   - `getTeacher()` returning `User` object → Use Option 1 (already uncommented)
   - `getCourse()` returning `Course` object → Use Option 1 (already uncommented)

2. **If ClassEntity has:**
   - `getTeacherId()` returning `Long` → Uncomment Option 2 and comment Option 1
   - `getCourseId()` returning `Long` → Uncomment Option 2 and comment Option 1

### For getMyAssignments() method:

**Fixed the sorting bug** - Changed from comparing String dates to comparing LocalDateTime objects.

---

## ✅ What Was Fixed:

1. **AssignmentController.createAssignment()**: Fixed teacher ownership check and courseId extraction from class
2. **AssignmentController.getAssignmentsByClass()**: Fixed to resolve classId → courseId (was using classId as courseId)
3. **AssignmentDTO**: Added `classId` field and fixed constructor defaults
4. **AssignmentController.getMyAssignments()**: Fixed date sorting bug

All code is ready to paste! Just check your ClassEntity structure and uncomment the correct option.

