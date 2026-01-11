package com.elearnhub.teacher_service.Controller;

import com.elearnhub.teacher_service.entity.Grade;
import com.elearnhub.teacher_service.service.GradeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/grades")
public class GradeController {
    @Autowired
    private GradeService gradeService;

    @PostMapping
    public ResponseEntity<Grade> createGrade(@Valid @RequestBody Grade grade) {
        return ResponseEntity.ok(gradeService.createGrade(grade));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Grade> getGradeById(@PathVariable Long id) {
        return gradeService.getGradeById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Grade> updateGrade(@PathVariable Long id, @Valid @RequestBody Grade grade) {
        return ResponseEntity.ok(gradeService.updateGrade(id, grade));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGrade(@PathVariable Long id) {
        gradeService.deleteGrade(id);
        return ResponseEntity.noContent().build();
    }
}