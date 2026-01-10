package com.elearnhub.teacher_service.service;

import com.elearnhub.teacher_service.dto.AssignmentDTO;
import com.elearnhub.teacher_service.entity.Assignment;
import com.elearnhub.teacher_service.repository.AssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AssignmentServiceImpl implements AssignmentService {

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Override
    public AssignmentDTO createAssignment(AssignmentDTO assignmentDTO) {
        Assignment assignment = convertToEntity(assignmentDTO);
        Assignment saved = assignmentRepository.save(assignment);
        return convertToDTO(saved);
    }

    @Override
    public AssignmentDTO getAssignmentById(Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found with id: " + assignmentId));
        return convertToDTO(assignment);
    }

    @Override
    public List<AssignmentDTO> getAssignmentsByCourse(Long courseId) {
        List<Assignment> assignments = assignmentRepository.findByCourseId(courseId);
        return assignments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AssignmentDTO> getAssignmentsByClass(Long classId) {
        List<Assignment> assignments = assignmentRepository.findByClassId(classId);
        return assignments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AssignmentDTO> getAllAssignments() {
        List<Assignment> assignments = assignmentRepository.findAll();
        return assignments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
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

    @Override
    public void deleteAssignment(Long assignmentId) {
        if (!assignmentRepository.existsById(assignmentId)) {
            throw new RuntimeException("Assignment not found with id: " + assignmentId);
        }
        assignmentRepository.deleteById(assignmentId);
    }

    // Helper methods
    private AssignmentDTO convertToDTO(Assignment assignment) {
        AssignmentDTO dto = new AssignmentDTO();
        dto.setId(assignment.getId());
        dto.setTitle(assignment.getTitle());
        dto.setDescription(assignment.getDescription());
        dto.setDueDate(assignment.getDueDate());
        dto.setMaxGrade(assignment.getMaxGrade());
        dto.setCourseId(assignment.getCourseId());
        dto.setClassId(assignment.getClassId());
        dto.setWeight(assignment.getWeight());
        dto.setAllowLateSubmission(assignment.getAllowLateSubmission());
        dto.setLatePenalty(assignment.getLatePenalty());
        dto.setAdditionalInstructions(assignment.getAdditionalInstructions());
        dto.setStatus(assignment.getStatus());
        dto.setCreatedAt(assignment.getCreatedAt());
        dto.setUpdatedAt(assignment.getUpdatedAt());
        return dto;
    }

    private Assignment convertToEntity(AssignmentDTO dto) {
        Assignment assignment = new Assignment();
        assignment.setTitle(dto.getTitle());
        assignment.setDescription(dto.getDescription());
        assignment.setDueDate(dto.getDueDate());
        assignment.setMaxGrade(dto.getMaxGrade());
        assignment.setCourseId(dto.getCourseId());
        assignment.setClassId(dto.getClassId());
        assignment.setWeight(dto.getWeight());
        assignment.setAllowLateSubmission(dto.getAllowLateSubmission() != null ? dto.getAllowLateSubmission() : false);
        assignment.setLatePenalty(dto.getLatePenalty());
        assignment.setAdditionalInstructions(dto.getAdditionalInstructions());
        assignment.setStatus(dto.getStatus() != null && !dto.getStatus().isEmpty() ? dto.getStatus() : "published");
        return assignment;
    }
}
