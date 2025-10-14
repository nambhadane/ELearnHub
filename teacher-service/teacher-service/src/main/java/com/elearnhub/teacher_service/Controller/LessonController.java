package com.elearnhub.teacher_service.Controller;

import com.elearnhub.teacher_service.dto.LessonDTO;
import com.elearnhub.teacher_service.service.LessonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/lessons")
public class LessonController {

    @Autowired
    private LessonService lessonService;

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<LessonDTO> uploadLesson(
            @RequestParam Long classId,
            @RequestParam String title,
            @RequestParam String content,
            @RequestPart MultipartFile file) throws IOException {
        LessonDTO lessonDTO = lessonService.uploadLesson(classId, title, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(lessonDTO);
    }

    @GetMapping("/class/{classId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<LessonDTO>> getLessonsByClass(@PathVariable Long classId) {
        List<LessonDTO> lessons = lessonService.getLessonsByClass(classId);
        return ResponseEntity.ok(lessons);
    }
}