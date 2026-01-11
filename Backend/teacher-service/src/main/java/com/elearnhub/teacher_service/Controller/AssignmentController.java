package com.elearnhub.teacher_service.Controller;

import com.elearnhub.teacher_service.dto.AssignmentDTO;
import com.elearnhub.teacher_service.dto.StudentAssignmentDTO;
import com.elearnhub.teacher_service.dto.SubmissionDTO;
import com.elearnhub.teacher_service.service.AssignmentService;
import com.elearnhub.teacher_service.service.UserService;
import com.elearnhub.teacher_service.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/assignments")
@CrossOrigin(origins = {"http://localhost:8081", "http://localhost:5173"}, allowCredentials = "true")
public class AssignmentController {

    @Autowired
    private AssignmentService assignmentService;
    
    @Autowired
    private UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<?> createAssignment(@RequestBody AssignmentDTO assignmentDTO) {
        try {
            AssignmentDTO created = assignmentService.createAssignment(assignmentDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(created);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to create assignment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(error);
        }
    }

    @GetMapping("/{assignmentId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT') or hasRole('ADMIN')")
    public ResponseEntity<?> getAssignmentById(@PathVariable Long assignmentId) {
        try {
            AssignmentDTO assignment = assignmentService.getAssignmentById(assignmentId);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(assignment);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(error);
        }
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT') or hasRole('ADMIN')")
    public ResponseEntity<?> getAssignmentsByCourse(@PathVariable Long courseId) {
        try {
            List<AssignmentDTO> assignments = assignmentService.getAssignmentsByCourse(courseId);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(assignments);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to fetch assignments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(error);
        }
    }

    @GetMapping("/class/{classId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT') or hasRole('ADMIN')")
    public ResponseEntity<?> getAssignmentsByClass(@PathVariable Long classId) {
        try {
            List<AssignmentDTO> assignments = assignmentService.getAssignmentsByClass(classId);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(assignments);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to fetch assignments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(error);
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllAssignments() {
        try {
            List<AssignmentDTO> assignments = assignmentService.getAllAssignments();
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(assignments);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to fetch assignments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(error);
        }
    }

    // ✅ FIXED: Get assignments for the current student with submission status
    @GetMapping("/my-assignments")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getMyAssignments() {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            // Get user by username
            User currentUser = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));
            
            // Get assignments with submission status for this student
            List<StudentAssignmentDTO> assignments = assignmentService.getStudentAssignmentsWithStatus(currentUser.getId());
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(assignments);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to fetch your assignments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(error);
        }
    }

    @PutMapping("/{assignmentId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateAssignment(
            @PathVariable Long assignmentId,
            @RequestBody AssignmentDTO assignmentDTO) {
        try {
            AssignmentDTO updated = assignmentService.updateAssignment(assignmentId, assignmentDTO);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(updated);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to update assignment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(error);
        }
    }

    @DeleteMapping("/{assignmentId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteAssignment(@PathVariable Long assignmentId) {
        try {
            assignmentService.deleteAssignment(assignmentId);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("message", "Assignment deleted successfully"));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to delete assignment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(error);
        }
    }

    // ===== SUBMISSION ENDPOINTS =====

    @PostMapping(value = "/submissions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> submitAssignment(
            @RequestParam("assignmentId") Long assignmentId,
            @RequestParam("content") String content,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            // Get user by username
            User currentUser = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));
            
            // Create submission DTO
            SubmissionDTO submissionDTO = new SubmissionDTO();
            submissionDTO.setAssignmentId(assignmentId);
            submissionDTO.setStudentId(currentUser.getId());
            submissionDTO.setContent(content);
            
            // Handle file upload if present
            if (file != null && !file.isEmpty()) {
                // For now, just store the filename - file handling needs more setup
                submissionDTO.setFilePath(file.getOriginalFilename());
            }
            
            // Save the submission
            SubmissionDTO savedSubmission = assignmentService.saveSubmission(submissionDTO);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(savedSubmission);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to submit assignment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(error);
        }
    }

    @GetMapping("/{assignmentId}/submissions")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<?> getSubmissionsByAssignment(@PathVariable Long assignmentId) {
        try {
            List<SubmissionDTO> submissions = assignmentService.getSubmissionsByAssignment(assignmentId);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(submissions);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to fetch submissions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(error);
        }
    }

    @GetMapping("/{assignmentId}/submission/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getMySubmission(@PathVariable Long assignmentId) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            // Get user by username
            User currentUser = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));
            
            // Get student's submission for this assignment
            SubmissionDTO submission = assignmentService.getSubmissionByStudentAndAssignment(
                    currentUser.getId(), assignmentId);
            
            if (submission == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Map.of("message", "No submission found"));
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(submission);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to fetch submission: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(error);
        }
    }

    @PutMapping("/submissions/{submissionId}/grade")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<?> gradeSubmission(
            @PathVariable Long submissionId,
            @RequestBody Map<String, Object> gradeData) {
        try {
            // Validate request data
            if (gradeData == null || gradeData.isEmpty()) {
                throw new RuntimeException("Grade data is required");
            }
            
            // Validate and extract score (check both "score" and "grade" fields)
            Object scoreObj = gradeData.get("score");
            if (scoreObj == null) {
                scoreObj = gradeData.get("grade"); // Try "grade" field as fallback
            }
            if (scoreObj == null) {
                throw new RuntimeException("Score/grade is required");
            }
            
            Double score;
            try {
                score = Double.valueOf(scoreObj.toString());
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid score format: " + scoreObj);
            }
            
            // Extract feedback (optional)
            String feedback = gradeData.get("feedback") != null ? gradeData.get("feedback").toString() : "";
            
            SubmissionDTO gradedSubmission = assignmentService.gradeSubmission(submissionId, score, feedback);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(gradedSubmission);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to grade submission: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(error);
        }
    }

    @GetMapping("/submissions/{submissionId}/file")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT') or hasRole('ADMIN')")
    public ResponseEntity<?> downloadSubmissionFile(@PathVariable Long submissionId) {
        try {
            // Get the submission
            SubmissionDTO submission = assignmentService.getSubmissionById(submissionId);
            
            if (submission == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Submission not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(error);
            }
            
            // Priority 1: If there's a file path, inform about file storage limitation
            if (submission.getFilePath() != null && !submission.getFilePath().trim().isEmpty()) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "File download not available");
                response.put("originalFilename", submission.getFilePath());
                response.put("explanation", "The uploaded file '" + submission.getFilePath() + "' cannot be downloaded because file storage is not implemented on the server.");
                response.put("suggestion", "Please ask the student to resubmit the file or provide the content as text.");
                response.put("hasTextContent", submission.getContent() != null && !submission.getContent().trim().isEmpty() ? "yes" : "no");
                
                return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(response);
            }
            
            // Priority 2: If there's text content (and no file), create a downloadable text file
            if (submission.getContent() != null && !submission.getContent().trim().isEmpty()) {
                String filename = "submission_" + submissionId + ".txt";
                
                return ResponseEntity.ok()
                        .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                        .contentType(MediaType.TEXT_PLAIN)
                        .body(submission.getContent());
            }
            
            // No content or file
            Map<String, String> response = new HashMap<>();
            response.put("message", "No downloadable content found");
            response.put("submissionId", submissionId.toString());
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
                    
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to download file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(error);
        }
    }

    @GetMapping("/submissions/{submissionId}/details")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT') or hasRole('ADMIN')")
    public ResponseEntity<?> getSubmissionDetails(@PathVariable Long submissionId) {
        try {
            SubmissionDTO submission = assignmentService.getSubmissionById(submissionId);
            
            if (submission == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Submission not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(error);
            }
            
            Map<String, Object> details = new HashMap<>();
            details.put("submissionId", submissionId);
            details.put("hasTextContent", submission.getContent() != null && !submission.getContent().trim().isEmpty());
            details.put("hasFileAttachment", submission.getFilePath() != null && !submission.getFilePath().trim().isEmpty());
            details.put("textContent", submission.getContent());
            details.put("fileName", submission.getFilePath());
            details.put("submittedAt", submission.getSubmittedAt());
            details.put("studentName", submission.getStudentName());
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(details);
                    
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to get submission details: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(error);
        }
    }
}