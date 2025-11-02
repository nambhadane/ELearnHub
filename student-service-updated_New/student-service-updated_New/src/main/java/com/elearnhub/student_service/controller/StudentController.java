package com.elearnhub.student_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.elearnhub.student_service.dto.AssignmentDTO;
import com.elearnhub.student_service.dto.ClassDTO;
import com.elearnhub.student_service.dto.LessonDTO;
import com.elearnhub.student_service.dto.SubmissionDTO;
import com.elearnhub.student_service.service.StudentService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private StudentService studentService;

	/*
	 * @GetMapping("/classes")
	 * 
	 * @PreAuthorize("hasRole('STUDENT')") public ResponseEntity<List<ClassDTO>>
	 * getStudentClasses() { List<ClassDTO> classes =
	 * studentService.getClassesForStudent(); return ResponseEntity.ok(classes); }
	 */
    
    @GetMapping("/classes")
    @PreAuthorize("hasRole('STUDENT')")
    public String getClasses() {
        return "List of classes for student";
    }

    @GetMapping("/classes/{classId}/lessons")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<LessonDTO>> getLessonsByClass(@PathVariable Long classId) {
        List<LessonDTO> lessons = studentService.getLessonsByClass(classId);
        return ResponseEntity.ok(lessons);
    }

    @GetMapping("/classes/{classId}/assignments")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<AssignmentDTO>> getAssignmentsByClass(@PathVariable Long classId) {
        List<AssignmentDTO> assignments = studentService.getAssignmentsByClass(classId);
        return ResponseEntity.ok(assignments);
    }

    @PostMapping("/assignments/{assignmentId}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<SubmissionDTO> submitAssignment(@PathVariable Long assignmentId, @RequestPart MultipartFile file) throws IOException {
        SubmissionDTO submissionDTO = studentService.submitAssignment(assignmentId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(submissionDTO);
    }

    @GetMapping("/submissions")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<SubmissionDTO>> getStudentSubmissions() {
        List<SubmissionDTO> submissions = studentService.getStudentSubmissions();
        return ResponseEntity.ok(submissions);
    }
}