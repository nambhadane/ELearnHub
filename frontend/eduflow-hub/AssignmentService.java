package com.elearnhub.teacher_service.service;

import com.elearnhub.teacher_service.dto.AssignmentDTO;
import java.util.List;

public interface AssignmentService {
    AssignmentDTO createAssignment(AssignmentDTO assignmentDTO);
    AssignmentDTO getAssignmentById(Long assignmentId);
    List<AssignmentDTO> getAssignmentsByCourse(Long courseId);
    List<AssignmentDTO> getAssignmentsByClass(Long classId);
    List<AssignmentDTO> getAllAssignments();
    AssignmentDTO updateAssignment(Long assignmentId, AssignmentDTO assignmentDTO);
    void deleteAssignment(Long assignmentId);
}
