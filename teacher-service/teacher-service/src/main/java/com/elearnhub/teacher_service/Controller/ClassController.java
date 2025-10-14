package com.elearnhub.teacher_service.Controller;

import com.elearnhub.teacher_service.dto.ClassDTO;
import com.elearnhub.teacher_service.service.ClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/classes")
public class ClassController {

    @Autowired
    private ClassService classService;

    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ClassDTO> createClass(
            @RequestParam Long teacherId,
            @RequestParam Long courseId,
            @RequestParam String name) {
        ClassDTO classDTO = classService.createClass(teacherId, courseId, name);
        return ResponseEntity.status(HttpStatus.CREATED).body(classDTO);
    }

    @GetMapping("/teacher/{teacherId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<ClassDTO>> getClassesByTeacher(@PathVariable Long teacherId) {
        List<ClassDTO> classes = classService.getClassesByTeacher(teacherId);
        return ResponseEntity.ok(classes);
    }
}