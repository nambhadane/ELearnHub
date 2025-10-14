package com.elearnhub.teacher_service.Controller;

import com.elearnhub.teacher_service.dto.AssignmentDTO;
import com.elearnhub.teacher_service.entity.Assignment;
import com.elearnhub.teacher_service.service.AssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/assignments")
public class AssignmentController {

    @Autowired
    private AssignmentService assignmentService;

    @GetMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<AssignmentDTO>> getAllAssignments() {
        List<AssignmentDTO> assignments = assignmentService.getAllAssignments();
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<AssignmentDTO>> getAssignmentsByCourse(@PathVariable Long courseId) {
        List<AssignmentDTO> assignments = assignmentService.getAssignmentsByCourseId(courseId);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<AssignmentDTO> getAssignmentById(@PathVariable Long id) {
        AssignmentDTO assignment = assignmentService.getAssignmentById(id);
        if (assignment != null) {
            return ResponseEntity.ok(assignment);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<AssignmentDTO> createAssignment(@RequestBody AssignmentDTO assignmentDTO) {
        AssignmentDTO savedAssignment = assignmentService.createAssignment(assignmentDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAssignment);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<AssignmentDTO> updateAssignment(@PathVariable Long id, @RequestBody AssignmentDTO assignmentDTO) {
        try {
            AssignmentDTO updatedAssignment = assignmentService.updateAssignment(id, assignmentDTO);
            return ResponseEntity.ok(updatedAssignment);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
        try {
            assignmentService.deleteAssignment(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}