package com.elearnhub.teacher_service.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import com.elearnhub.teacher_service.dto.AssignmentDTO;
import com.elearnhub.teacher_service.dto.StudentAssignmentDTO;
import com.elearnhub.teacher_service.dto.SubmissionDTO;
import com.elearnhub.teacher_service.entity.Assignment;
import com.elearnhub.teacher_service.entity.ClassEntity;
import com.elearnhub.teacher_service.entity.Submission;
import com.elearnhub.teacher_service.entity.User;
import com.elearnhub.teacher_service.repository.AssignmentRepository;
import com.elearnhub.teacher_service.repository.ClassEntityRepository;
import com.elearnhub.teacher_service.repository.SubmissionRepository;
//import com.elearnhub.teacher_service.repository.ClassRepository;
import com.elearnhub.teacher_service.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
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
    
    @Autowired
    private ClassService classService;
    
    @Autowired
    private ClassEntityRepository classRepository;

    public AssignmentDTO createAssignment(AssignmentDTO assignmentDTO) {
        // ✅ FIX: If classId is provided but courseId is null, get courseId from class
        if (assignmentDTO.getClassId() != null && assignmentDTO.getCourseId() == null) {
            Optional<ClassEntity> classOpt = classService.getClassById(assignmentDTO.getClassId());
            if (classOpt.isPresent()) {
                ClassEntity classEntity = classOpt.get();
                if (classEntity.getCourse() != null) {
                    assignmentDTO.setCourseId(classEntity.getCourse().getId());
                } else {
                    throw new RuntimeException("Class does not have an associated course");
                }
            } else {
                throw new RuntimeException("Class not found with id: " + assignmentDTO.getClassId());
            }
        }
        
        // Validate that courseId is not null
        if (assignmentDTO.getCourseId() == null) {
            throw new RuntimeException("Course ID is required for assignment creation");
        }
        
        Assignment assignment = new Assignment();
        assignment.setTitle(assignmentDTO.getTitle());
        assignment.setDescription(assignmentDTO.getDescription());
        assignment.setDueDate(assignmentDTO.getDueDate());
        assignment.setMaxGrade(assignmentDTO.getMaxGrade());
        assignment.setCourseId(assignmentDTO.getCourseId());
        assignment.setClassId(assignmentDTO.getClassId()); // ✅ Also set classId
        
        // NEW: Set new fields
        assignment.setWeight(assignmentDTO.getWeight());
        assignment.setAllowLateSubmission(assignmentDTO.getAllowLateSubmission() != null ? assignmentDTO.getAllowLateSubmission() : false);
        assignment.setLatePenalty(assignmentDTO.getLatePenalty());
        assignment.setAdditionalInstructions(assignmentDTO.getAdditionalInstructions());
        assignment.setStatus(assignmentDTO.getStatus() != null && !assignmentDTO.getStatus().isEmpty() 
            ? assignmentDTO.getStatus() 
            : "published");
        
        Assignment saved = assignmentRepository.save(assignment);
        return convertToDTO(saved);
    }

    public List<AssignmentDTO> getAssignmentsByClass(Long classId) {
        List<Assignment> assignments = assignmentRepository.findByClassId(classId);
        return assignments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public AssignmentDTO updateAssignment(Long assignmentId, AssignmentDTO assignmentDTO) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found with id: " + assignmentId));
        
        assignment.setTitle(assignmentDTO.getTitle());
        assignment.setDescription(assignmentDTO.getDescription());
        assignment.setDueDate(assignmentDTO.getDueDate());
        assignment.setMaxGrade(assignmentDTO.getMaxGrade());
        assignment.setWeight(assignmentDTO.getWeight());
        assignment.setAllowLateSubmission(assignmentDTO.getAllowLateSubmission());
        assignment.setLatePenalty(assignmentDTO.getLatePenalty());
        assignment.setAdditionalInstructions(assignmentDTO.getAdditionalInstructions());
        assignment.setStatus(assignmentDTO.getStatus());
        
        Assignment updated = assignmentRepository.save(assignment);
        return convertToDTO(updated);
    }

    public List<AssignmentDTO> getAllAssignments() {
        List<Assignment> assignments = assignmentRepository.findAll();
        return assignments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<AssignmentDTO> getAssignmentsByCourse(Long courseId) {
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
        // Get Assignment entity
        Assignment assignment = assignmentRepository.findById(submissionDTO.getAssignmentId())
                .orElseThrow(() -> new RuntimeException("Assignment not found with id: " + submissionDTO.getAssignmentId()));
        
        // Get User entity (student)
        User student = userService.getUserById(submissionDTO.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + submissionDTO.getStudentId()));
        
        Submission submission = new Submission();
        submission.setAssignment(assignment);
        submission.setStudent(student);
        submission.setContent(submissionDTO.getContent());
        submission.setFilePath(submissionDTO.getFilePath());
        submission.setSubmittedAt(LocalDateTime.now());
        
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
            String studentName = submission.getStudent().getName() != null && !submission.getStudent().getName().trim().isEmpty()
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
        dto.setClassId(assignment.getClassId()); // ✅ Include classId
        
        // NEW: Map new fields
        dto.setWeight(assignment.getWeight());
        dto.setAllowLateSubmission(assignment.getAllowLateSubmission());
        dto.setLatePenalty(assignment.getLatePenalty());
        dto.setAdditionalInstructions(assignment.getAdditionalInstructions());
        dto.setStatus(assignment.getStatus());
        // Note: createdAt and updatedAt are set automatically by @PrePersist and @PreUpdate
        if (assignment.getCreatedAt() != null) {
            dto.setCreatedAt(assignment.getCreatedAt());
        }
        if (assignment.getUpdatedAt() != null) {
            dto.setUpdatedAt(assignment.getUpdatedAt());
        }
        
        return dto;
    }

    public SubmissionDTO getSubmissionByStudentAndAssignment(Long studentId, Long assignmentId) {
        // ✅ FIX: Handle multiple submissions by getting the most recent one
        List<Submission> submissions = submissionRepository.findByAssignmentIdAndStudentId(assignmentId, studentId);
        
        if (submissions == null || submissions.isEmpty()) {
            return null;
        }
        
        // If multiple submissions exist, get the most recent one (by submittedAt)
        Submission submission = submissions.stream()
                .filter(s -> s.getSubmittedAt() != null)
                .max(Comparator.comparing(Submission::getSubmittedAt))
                .orElse(submissions.get(0)); // Fallback to first if no dates
        
        return convertSubmissionToDTO(submission);
    }

    public AssignmentDTO convertToAssignmentDTO(Assignment assignment) {
        return convertToDTO(assignment);
    }

    public SubmissionDTO getSubmissionById(Long submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found with id: " + submissionId));
        return convertSubmissionToDTO(submission);
    }

    public List<AssignmentDTO> getAssignmentsForStudent(Long studentId) {
        // Get all classes the student is enrolled in
        List<ClassEntity> studentClasses = classRepository.findClassesByStudentId(studentId);
        
        // Get all assignments from those classes
        List<Assignment> assignments = new ArrayList<>();
        for (ClassEntity classEntity : studentClasses) {
            List<Assignment> classAssignments = assignmentRepository.findByClassId(classEntity.getId());
            assignments.addAll(classAssignments);
        }
        
        // Convert to DTOs and return
        return assignments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<StudentAssignmentDTO> getStudentAssignmentsWithStatus(Long studentId) {
        // Get all classes the student is enrolled in
        List<ClassEntity> studentClasses = classRepository.findClassesByStudentId(studentId);
        
        List<StudentAssignmentDTO> studentAssignments = new ArrayList<>();
        
        for (ClassEntity classEntity : studentClasses) {
            List<Assignment> classAssignments = assignmentRepository.findByClassId(classEntity.getId());
            
            for (Assignment assignment : classAssignments) {
                StudentAssignmentDTO studentAssignment = new StudentAssignmentDTO();
                
                // Set assignment details
                studentAssignment.setId(assignment.getId());
                studentAssignment.setTitle(assignment.getTitle());
                studentAssignment.setDescription(assignment.getDescription());
                studentAssignment.setDueDate(assignment.getDueDate());
                studentAssignment.setMaxGrade(assignment.getMaxGrade());
                studentAssignment.setCourseId(assignment.getCourseId());
                studentAssignment.setClassName(classEntity.getName());
                
                // Check submission status
                SubmissionDTO submission = getSubmissionByStudentAndAssignment(studentId, assignment.getId());
                
                if (submission == null) {
                    studentAssignment.setStatus("pending");
                } else if (submission.getGrade() != null) {
                    studentAssignment.setStatus("graded");
                    studentAssignment.setSubmissionId(submission.getId());
                    studentAssignment.setSubmittedAt(submission.getSubmittedAt());
                    studentAssignment.setGrade(submission.getGrade());
                    studentAssignment.setFeedback(submission.getFeedback());
                } else {
                    studentAssignment.setStatus("submitted");
                    studentAssignment.setSubmissionId(submission.getId());
                    studentAssignment.setSubmittedAt(submission.getSubmittedAt());
                }
                
                studentAssignments.add(studentAssignment);
            }
        }
        
        return studentAssignments;
    }
}