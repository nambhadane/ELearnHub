package com.elearnhub.teacher_service.service;

import com.elearnhub.teacher_service.entity.Grade;
import com.elearnhub.teacher_service.repository.GradeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GradeService {
    @Autowired
    private GradeRepository gradeRepository;

    public Grade createGrade(Grade grade) {
        return gradeRepository.save(grade);
    }

    public Optional<Grade> getGradeById(Long id) {
        return gradeRepository.findById(id);
    }

    public Grade updateGrade(Long id, Grade grade) {
        Grade existingGrade = gradeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Grade not found with id: " + id));
        existingGrade.setScore(grade.getScore());
        existingGrade.setFeedback(grade.getFeedback());
        existingGrade.setSubmissionId(grade.getSubmissionId());
        return gradeRepository.save(existingGrade);
    }

    public void deleteGrade(Long id) {
        gradeRepository.deleteById(id);
    }
}