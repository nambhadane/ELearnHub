package com.elearnhub.teacher_service.service;

import com.elearnhub.teacher_service.dto.*;
import com.elearnhub.teacher_service.entity.Quiz;

import java.util.List;

public interface QuizService {
    // Teacher operations
    QuizDTO createQuiz(QuizDTO quizDTO);
    QuizDTO updateQuiz(Long quizId, QuizDTO quizDTO);
    void deleteQuiz(Long quizId);
    QuizDTO getQuizById(Long quizId, boolean includeAnswers);
    List<QuizDTO> getQuizzesByClass(Long classId);
    QuizDTO publishQuiz(Long quizId);
    
    // Question management
    QuestionDTO addQuestion(Long quizId, QuestionDTO questionDTO);
    QuestionDTO updateQuestion(Long questionId, QuestionDTO questionDTO);
    void deleteQuestion(Long questionId);
    
    // Student operations
    List<QuizDTO> getAvailableQuizzes(Long classId, Long studentId);
    QuizAttemptDTO startQuizAttempt(Long quizId, Long studentId);
    QuizAttemptDTO submitQuizAttempt(Long attemptId, List<StudentAnswerDTO> answers);
    QuizAttemptDTO getAttemptById(Long attemptId, Long studentId);
    List<QuizAttemptDTO> getStudentAttempts(Long quizId, Long studentId);
    
    // Teacher view of attempts
    List<QuizAttemptDTO> getAllAttemptsForQuiz(Long quizId);
    QuizAttemptDTO gradeShortAnswer(Long answerId, Integer marks);
}
