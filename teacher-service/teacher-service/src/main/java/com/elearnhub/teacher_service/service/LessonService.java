package com.elearnhub.teacher_service.service;

import com.elearnhub.teacher_service.dto.LessonDTO;
import com.elearnhub.teacher_service.entity.ClassEntity;
import com.elearnhub.teacher_service.entity.Lesson;
import com.elearnhub.teacher_service.repository.ClassEntityRepository;
import com.elearnhub.teacher_service.repository.LessonRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LessonService {

    @Autowired
    private LessonRepository lessonRepository;
    @Autowired
    private ClassEntityRepository classEntityRepository;

    private final String UPLOAD_DIR = "uploads/"; // Directory to store files (adjust as needed)

    public LessonDTO uploadLesson(Long classId, String title, MultipartFile file) throws IOException {
        ClassEntity classEntity = classEntityRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Save file to disk
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);

        // Create and save lesson
        Lesson lesson = new Lesson(title, filePath.toString(), classEntity);
        Lesson savedLesson = lessonRepository.save(lesson);

        return new LessonDTO(savedLesson.getId(), savedLesson.getTitle(), savedLesson.getFilePath(), classId);
    }

    public List<LessonDTO> getLessonsByClass(Long classId) {
        List<Lesson> lessons = lessonRepository.findByClassEntityId(classId);
        return lessons.stream()
                .map(l -> new LessonDTO(l.getId(), l.getTitle(), l.getFilePath(), classId))
                .collect(Collectors.toList());
    }
}