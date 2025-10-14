package com.elearnhub.teacher_service.service;

import com.elearnhub.teacher_service.dto.ClassDTO;
import com.elearnhub.teacher_service.entity.ClassEntity;
import com.elearnhub.teacher_service.entity.Course;
import com.elearnhub.teacher_service.entity.User;
import com.elearnhub.teacher_service.repository.ClassEntityRepository;
import com.elearnhub.teacher_service.repository.CourseRepository;
import com.elearnhub.teacher_service.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClassService {

    @Autowired
    private ClassEntityRepository classEntityRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CourseRepository courseRepository;

    public ClassDTO createClass(Long teacherId, Long courseId, String name) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        ClassEntity classEntity = new ClassEntity(name, teacher, course);
        ClassEntity savedClass = classEntityRepository.save(classEntity);
        return new ClassDTO(savedClass.getId(), savedClass.getName(), teacherId, courseId);
    }

    public List<ClassDTO> getClassesByTeacher(Long teacherId) {
        List<ClassEntity> classes = classEntityRepository.findByTeacherId(teacherId);
        return classes.stream()
                .map(classEntity -> new ClassDTO(
                        classEntity.getId(),
                        classEntity.getName(),
                        classEntity.getTeacher().getId(),
                        classEntity.getCourse().getId()
                ))
                .collect(Collectors.toList());
    }
}