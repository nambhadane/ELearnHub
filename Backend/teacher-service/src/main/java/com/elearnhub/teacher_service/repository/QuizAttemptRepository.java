package com.elearnhub.teacher_service.repository;

import com.elearnhub.teacher_service.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findByQuizIdAndStudentId(Long quizId, Long studentId);
    List<QuizAttempt> findByQuizId(Long quizId);
    List<QuizAttempt> findByStudentId(Long studentId);
    
    @Query("SELECT COUNT(a) FROM QuizAttempt a WHERE a.quizId = ?1 AND a.studentId = ?2")
    Integer countAttemptsByQuizAndStudent(Long quizId, Long studentId);
    
    @Query("SELECT MAX(a.score) FROM QuizAttempt a WHERE a.quizId = ?1 AND a.studentId = ?2 AND a.status = 'SUBMITTED'")
    Optional<Integer> findBestScoreByQuizAndStudent(Long quizId, Long studentId);
    
    Optional<QuizAttempt> findByQuizIdAndStudentIdAndStatus(Long quizId, Long studentId, String status);
}
