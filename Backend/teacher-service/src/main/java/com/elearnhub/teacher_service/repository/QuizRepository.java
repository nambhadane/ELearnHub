package com.elearnhub.teacher_service.repository;

import com.elearnhub.teacher_service.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    
    // Find quizzes by course ID (updated from classId to courseId)
    List<Quiz> findByCourseId(Long courseId);
    
    // Find quizzes by course ID and status
    List<Quiz> findByCourseIdAndStatus(Long courseId, String status);
    
    // Find quizzes by status
    List<Quiz> findByStatus(String status);
    
    // Backward compatibility methods using native SQL
    @Query(value = "SELECT * FROM quizzes WHERE course_id = :classId", nativeQuery = true)
    List<Quiz> findByClassId(@Param("classId") Long classId);
    
    @Query(value = "SELECT * FROM quizzes WHERE course_id = :classId AND status = :status", nativeQuery = true)
    List<Quiz> findByClassIdAndStatus(@Param("classId") Long classId, @Param("status") String status);
}
