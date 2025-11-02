package com.elearnhub.teacher_service.Controller;

import com.elearnhub.teacher_service.dto.AssignmentDTO;
import com.elearnhub.teacher_service.dto.SubmissionDTO;
import com.elearnhub.teacher_service.entity.Assignment;
import com.elearnhub.teacher_service.entity.Course;
import com.elearnhub.teacher_service.entity.User;
import com.elearnhub.teacher_service.service.AssignmentService;
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

            Optional<Course> courseOptional = courseService.getCourseById(assignmentDTO.getCourseId());
            if (courseOptional.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Course not found with id: " + assignmentDTO.getCourseId());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            Course course = courseOptional.get();
            if (!course.getTeacherId().equals(teacher.getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Unauthorized: Course does not belong to this teacher");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            AssignmentDTO createdAssignment = assignmentService.createAssignment(assignmentDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAssignment);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to create assignment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get assignments by class/course - validates Course exists
    @GetMapping("/class/{classId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<?> getAssignmentsByClass(
            @PathVariable Long classId,
            Authentication authentication) {
        try {
            Optional<Course> courseOptional = courseService.getCourseById(classId);
            if (courseOptional.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Course not found with id: " + classId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            Course course = courseOptional.get(); // FIXED: Extract course
            List<AssignmentDTO> assignments = assignmentService.getAssignmentsByClass(course.getId());
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
                    
                    // Store multiple file paths as comma-separated or JSON string
                    // Option 1: Comma-separated
                    finalSubmissionDTO.setFilePath(String.join(",", filePaths));
                    
                    // Option 2: JSON array (if you want to parse it later)
                    // ObjectMapper mapper = new ObjectMapper();
                    // finalSubmissionDTO.setFilePath(mapper.writeValueAsString(filePaths));
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
                String dueDateA = (String) a.get("dueDate");
                String dueDateB = (String) b.get("dueDate");
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