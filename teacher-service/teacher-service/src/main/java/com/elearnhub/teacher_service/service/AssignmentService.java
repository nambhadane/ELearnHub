package com.elearnhub.teacher_service.service;

import com.elearnhub.teacher_service.dto.AssignmentDTO;
import com.elearnhub.teacher_service.entity.Assignment;
import com.elearnhub.teacher_service.repository.AssignmentRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AssignmentService {

    @Autowired
    private AssignmentRepository assignmentRepository;

    public List<AssignmentDTO> getAllAssignments() {
        List<Assignment> assignments = assignmentRepository.findAll();
        return assignments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<AssignmentDTO> getAssignmentsByCourseId(Long courseId) {
        List<Assignment> assignments = assignmentRepository.findByCourseId(courseId);
        return assignments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public AssignmentDTO getAssignmentById(Long id) {
        Optional<Assignment> assignment = assignmentRepository.findById(id);
        return assignment.map(this::convertToDTO)
                .orElse(null);
    }

    public AssignmentDTO createAssignment(AssignmentDTO assignmentDTO) {
        Assignment assignment = convertToEntity(assignmentDTO);
        Assignment savedAssignment = assignmentRepository.save(assignment);
        return convertToDTO(savedAssignment);
    }

    public AssignmentDTO updateAssignment(Long id, AssignmentDTO assignmentDTO) {
        Optional<Assignment> existingAssignment = assignmentRepository.findById(id);
        if (existingAssignment.isPresent()) {
            Assignment updatedAssignment = existingAssignment.get();
            updatedAssignment.setTitle(assignmentDTO.getTitle());
            updatedAssignment.setDescription(assignmentDTO.getDescription());
            updatedAssignment.setDueDate(assignmentDTO.getDueDate());
            updatedAssignment.setMaxGrade(assignmentDTO.getMaxGrade());
            updatedAssignment.setCourseId(assignmentDTO.getCourseId());
            Assignment savedAssignment = assignmentRepository.save(updatedAssignment);
            return convertToDTO(savedAssignment);
        }
        throw new RuntimeException("Assignment not found with id: " + id);
    }

    public void deleteAssignment(Long id) {
        assignmentRepository.deleteById(id);
    }

    private AssignmentDTO convertToDTO(Assignment assignment) {
        return new AssignmentDTO(
                assignment.getId(),
                assignment.getCourseId(),
                assignment.getTitle(),
                assignment.getDescription(),
                assignment.getDueDate(),
                assignment.getMaxGrade()
        );
    }

    private Assignment convertToEntity(AssignmentDTO assignmentDTO) {
        Assignment assignment = new Assignment();
        assignment.setId(assignmentDTO.getId());
        assignment.setCourseId(assignmentDTO.getCourseId());
        assignment.setTitle(assignmentDTO.getTitle());
        assignment.setDescription(assignmentDTO.getDescription());
        assignment.setDueDate(assignmentDTO.getDueDate());
        assignment.setMaxGrade(assignmentDTO.getMaxGrade());
        return assignment;
    }
}