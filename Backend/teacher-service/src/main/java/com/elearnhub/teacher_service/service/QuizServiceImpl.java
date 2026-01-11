package com.elearnhub.teacher_service.service;

import com.elearnhub.teacher_service.dto.*;
import com.elearnhub.teacher_service.entity.*;
import com.elearnhub.teacher_service.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// Explicit import for Notification enum
import com.elearnhub.teacher_service.entity.Notification.NotificationType;

@Service
@Transactional
public class QuizServiceImpl implements QuizService {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private StudentAnswerRepository studentAnswerRepository;

    @Autowired
    private ClassEntityRepository classRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    // ============= TEACHER OPERATIONS =============

    @Override
    public QuizDTO createQuiz(QuizDTO quizDTO) {
        // ✅ DEBUG: Log the incoming request
        System.out.println("🎯 Creating quiz for class ID: " + quizDTO.getClassId());
        
        // Validate class exists and get course ID
        ClassEntity classEntity = classRepository.findById(quizDTO.getClassId())
                .orElseThrow(() -> new RuntimeException("Class not found with ID: " + quizDTO.getClassId()));

        // ✅ DEBUG: Log class details
        System.out.println("🎯 Found class: " + classEntity.getName());
        System.out.println("🎯 Class courseId field: " + classEntity.getCourseId());
        System.out.println("🎯 Class course object: " + (classEntity.getCourse() != null ? classEntity.getCourse().getName() : "null"));

        // Create quiz entity
        Quiz quiz = new Quiz();
        // ✅ FIX: Use course ID from the class entity
        Long courseId = classEntity.getCourseId();
        if (courseId == null && classEntity.getCourse() != null) {
            courseId = classEntity.getCourse().getId();
            System.out.println("🎯 Got courseId from course object: " + courseId);
        }
        if (courseId == null) {
            throw new RuntimeException("Class '" + classEntity.getName() + "' (ID: " + classEntity.getId() + ") must be associated with a course to create a quiz. Please link this class to a course first.");
        }
        
        System.out.println("🎯 Using courseId: " + courseId + " for quiz creation");
        quiz.setCourseId(courseId);
        quiz.setTitle(quizDTO.getTitle());
        quiz.setDescription(quizDTO.getDescription());
        quiz.setTimeLimit(quizDTO.getDuration());
        quiz.setMaxGrade(quizDTO.getTotalMarks());
        quiz.setPassingMarks(quizDTO.getPassingMarks());
        quiz.setStartTime(quizDTO.getStartTime());
        quiz.setDueDate(quizDTO.getEndTime());
        quiz.setMaxAttempts(quizDTO.getMaxAttempts() != null ? quizDTO.getMaxAttempts() : 1);
        quiz.setShuffleQuestions(quizDTO.getRandomizeQuestions() != null ? quizDTO.getRandomizeQuestions() : false);
        quiz.setShowResults(quizDTO.getShowResultsImmediately() != null ? quizDTO.getShowResultsImmediately() : true);
        quiz.setStatus(quizDTO.getStatus() != null ? quizDTO.getStatus() : "DRAFT");
        quiz.setCreatedAt(LocalDateTime.now());

        Quiz savedQuiz = quizRepository.save(quiz);

        // Create questions if provided
        if (quizDTO.getQuestions() != null && !quizDTO.getQuestions().isEmpty()) {
            int orderIndex = 0;
            for (QuestionDTO qDto : quizDTO.getQuestions()) {
                Question question = createQuestionFromDTO(qDto, savedQuiz, orderIndex++);
                questionRepository.save(question);
            }
        }

        return convertToQuizDTO(savedQuiz, true);
    }

    @Override
    public QuizDTO updateQuiz(Long quizId, QuizDTO quizDTO) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        // Check if quiz has attempts
        List<QuizAttempt> attempts = quizAttemptRepository.findByQuizId(quizId);
        if (!attempts.isEmpty() && "PUBLISHED".equals(quiz.getStatus())) {
            throw new RuntimeException("Cannot edit quiz that has been attempted by students");
        }

        // Update quiz fields
        quiz.setTitle(quizDTO.getTitle());
        quiz.setDescription(quizDTO.getDescription());
        quiz.setTimeLimit(quizDTO.getDuration());
        quiz.setMaxGrade(quizDTO.getTotalMarks());
        quiz.setPassingMarks(quizDTO.getPassingMarks());
        quiz.setStartTime(quizDTO.getStartTime());
        quiz.setDueDate(quizDTO.getEndTime());
        quiz.setMaxAttempts(quizDTO.getMaxAttempts());
        quiz.setShuffleQuestions(quizDTO.getRandomizeQuestions());
        quiz.setShowResults(quizDTO.getShowResultsImmediately());
        quiz.setStatus(quizDTO.getStatus());
        quiz.setUpdatedAt(LocalDateTime.now());

        Quiz updatedQuiz = quizRepository.save(quiz);
        return convertToQuizDTO(updatedQuiz, true);
    }

    @Override
    public void deleteQuiz(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        // Check if quiz has attempts
        List<QuizAttempt> attempts = quizAttemptRepository.findByQuizId(quizId);
        if (!attempts.isEmpty()) {
            throw new RuntimeException("Cannot delete quiz that has been attempted by students");
        }

        quizRepository.delete(quiz);
    }

    @Override
    public QuizDTO getQuizById(Long quizId, boolean includeAnswers) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        return convertToQuizDTO(quiz, includeAnswers);
    }

    @Override
    public List<QuizDTO> getQuizzesByClass(Long classId) {
        // ✅ FIX: Get the course ID from the class, then find quizzes by course ID
        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));
        
        Long courseId = classEntity.getCourseId();
        if (courseId == null && classEntity.getCourse() != null) {
            courseId = classEntity.getCourse().getId();
        }
        if (courseId == null) {
            // Return empty list if no course associated
            return new ArrayList<>();
        }
        
        List<Quiz> quizzes = quizRepository.findByCourseId(courseId);
        return quizzes.stream()
                .map(quiz -> convertToQuizDTO(quiz, false))
                .collect(Collectors.toList());
    }

    @Override
    public QuizDTO publishQuiz(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        if (quiz.getQuestions() == null || quiz.getQuestions().isEmpty()) {
            throw new RuntimeException("Cannot publish quiz without questions");
        }

        String oldStatus = quiz.getStatus();
        quiz.setStatus("PUBLISHED");
        quiz.setUpdatedAt(LocalDateTime.now());

        Quiz publishedQuiz = quizRepository.save(quiz);

        // Notify students if status changed to PUBLISHED
        if (!"PUBLISHED".equals(oldStatus)) {
            notifyStudentsAboutNewQuiz(publishedQuiz);
        }

        return convertToQuizDTO(publishedQuiz, true);
    }

    // ============= QUESTION MANAGEMENT =============

    @Override
    public QuestionDTO addQuestion(Long quizId, QuestionDTO questionDTO) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        int orderIndex = quiz.getQuestions() != null ? quiz.getQuestions().size() : 0;
        Question question = createQuestionFromDTO(questionDTO, quiz, orderIndex);
        Question savedQuestion = questionRepository.save(question);

        return convertToQuestionDTO(savedQuestion, true);
    }

    @Override
    public QuestionDTO updateQuestion(Long questionId, QuestionDTO questionDTO) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        question.setQuestionText(questionDTO.getQuestionText());
        question.setQuestionType(questionDTO.getQuestionType());
        question.setMarks(questionDTO.getMarks());
        question.setExplanation(questionDTO.getExplanation());

        // Update options for MCQ/Multiple Select
        if ("MULTIPLE_CHOICE".equals(questionDTO.getQuestionType())) {
            question.getOptions().clear();
            int optionIndex = 0;
            for (QuestionOptionDTO optDto : questionDTO.getOptions()) {
                QuestionOption option = new QuestionOption();
                option.setQuestion(question);
                option.setOptionText(optDto.getOptionText());
                option.setIsCorrect(optDto.getIsCorrect() != null ? optDto.getIsCorrect() : false);
                option.setOrderIndex(optionIndex++);
                question.getOptions().add(option);
            }
        } else if ("TRUE_FALSE".equals(questionDTO.getQuestionType()) || "SHORT_ANSWER".equals(questionDTO.getQuestionType())) {
            question.setCorrectAnswer(questionDTO.getCorrectAnswer());
        }

        Question updatedQuestion = questionRepository.save(question);
        return convertToQuestionDTO(updatedQuestion, true);
    }

    @Override
    public void deleteQuestion(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        questionRepository.delete(question);
    }

    // ============= STUDENT OPERATIONS =============

    @Override
    public List<QuizDTO> getAvailableQuizzes(Long classId, Long studentId) {
        List<Quiz> quizzes = quizRepository.findByCourseIdAndStatus(classId, "PUBLISHED");
        LocalDateTime now = LocalDateTime.now();

        return quizzes.stream()
                .map(quiz -> {
                    QuizDTO dto = convertToQuizDTO(quiz, false);
                    Integer attemptCount = quizAttemptRepository.countAttemptsByQuizAndStudent(quiz.getId(), studentId);
                    dto.setAttemptsUsed(attemptCount);
                    
                    // Check if quiz is within time window and has attempts remaining
                    boolean isWithinTimeWindow = quiz.getStartTime().isBefore(now) && quiz.getDueDate().isAfter(now);
                    boolean hasAttemptsRemaining = attemptCount < quiz.getMaxAttempts();
                    dto.setCanAttempt(isWithinTimeWindow && hasAttemptsRemaining);
                    
                    Optional<Integer> bestScore = quizAttemptRepository.findBestScoreByQuizAndStudent(quiz.getId(), studentId);
                    dto.setBestScore(bestScore.orElse(null));
                    
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public QuizAttemptDTO startQuizAttempt(Long quizId, Long studentId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        // Validate
        LocalDateTime now = LocalDateTime.now();
        if (quiz.getStartTime().isAfter(now)) {
            throw new RuntimeException("Quiz has not started yet");
        }
        if (quiz.getDueDate().isBefore(now)) {
            throw new RuntimeException("Quiz has ended");
        }

        Integer attemptCount = quizAttemptRepository.countAttemptsByQuizAndStudent(quizId, studentId);
        if (attemptCount >= quiz.getMaxAttempts()) {
            throw new RuntimeException("Maximum attempts reached");
        }

        // Check if there's an in-progress attempt
        Optional<QuizAttempt> existingAttempt = quizAttemptRepository
                .findByQuizIdAndStudentIdAndStatus(quizId, studentId, "IN_PROGRESS");
        if (existingAttempt.isPresent()) {
            return convertToQuizAttemptDTO(existingAttempt.get());
        }

        // Create new attempt
        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuizId(quizId);
        attempt.setStudentId(studentId);
        attempt.setAttemptNumber(attemptCount + 1);
        attempt.setStartedAt(LocalDateTime.now());
        attempt.setTotalMarks(quiz.getMaxGrade());
        attempt.setStatus("IN_PROGRESS");

        QuizAttempt savedAttempt = quizAttemptRepository.save(attempt);
        return convertToQuizAttemptDTO(savedAttempt);
    }

    @Override
    public QuizAttemptDTO submitQuizAttempt(Long attemptId, List<StudentAnswerDTO> answers) {
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));

        if (!"IN_PROGRESS".equals(attempt.getStatus())) {
            throw new RuntimeException("Attempt already submitted");
        }

        Quiz quiz = quizRepository.findById(attempt.getQuizId())
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        int totalScore = 0;
        boolean hasShortAnswer = false;

        // Save and grade answers
        for (StudentAnswerDTO answerDTO : answers) {
            Question question = questionRepository.findById(answerDTO.getQuestionId())
                    .orElseThrow(() -> new RuntimeException("Question not found"));

            StudentAnswer answer = new StudentAnswer();
            answer.setAttempt(attempt);
            answer.setQuestionId(answerDTO.getQuestionId());
            answer.setAnswerText(answerDTO.getAnswerText());
            answer.setSelectedOptionId(answerDTO.getSelectedOptionId());

            // Auto-grade objective questions
            if ("MULTIPLE_CHOICE".equals(question.getQuestionType())) {
                boolean isCorrect = question.getOptions().stream()
                        .anyMatch(opt -> opt.getId().equals(answerDTO.getSelectedOptionId()) && opt.getIsCorrect());
                answer.setIsCorrect(isCorrect);
                answer.setMarksAwarded(isCorrect ? question.getMarks() : 0);
                totalScore += answer.getMarksAwarded();
            } else if ("TRUE_FALSE".equals(question.getQuestionType())) {
                boolean isCorrect = question.getCorrectAnswer() != null && 
                        question.getCorrectAnswer().equalsIgnoreCase(answerDTO.getAnswerText());
                answer.setIsCorrect(isCorrect);
                answer.setMarksAwarded(isCorrect ? question.getMarks() : 0);
                totalScore += answer.getMarksAwarded();
            } else if ("SHORT_ANSWER".equals(question.getQuestionType())) {
                hasShortAnswer = true;
                answer.setMarksAwarded(0); // Requires manual grading
            }

            studentAnswerRepository.save(answer);
        }

        // Update attempt
        attempt.setSubmittedAt(LocalDateTime.now());
        attempt.setScore(totalScore);
        attempt.setStatus(hasShortAnswer ? "SUBMITTED" : "SUBMITTED");

        QuizAttempt submittedAttempt = quizAttemptRepository.save(attempt);

        // Send notification
        notificationService.createNotification(
                attempt.getStudentId(),
                "Quiz Submitted",
                "You have successfully submitted '" + quiz.getTitle() + "'",
                Notification.NotificationType.SUBMISSION,
                attemptId,
                "QUIZ_ATTEMPT"
        );

        return convertToQuizAttemptDTO(submittedAttempt);
    }

    @Override
    public QuizAttemptDTO getAttemptById(Long attemptId, Long studentId) {
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));

        if (!attempt.getStudentId().equals(studentId)) {
            throw new RuntimeException("Unauthorized");
        }

        return convertToQuizAttemptDTO(attempt);
    }

    @Override
    public List<QuizAttemptDTO> getStudentAttempts(Long quizId, Long studentId) {
        List<QuizAttempt> attempts = quizAttemptRepository.findByQuizIdAndStudentId(quizId, studentId);
        return attempts.stream()
                .map(this::convertToQuizAttemptDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<QuizAttemptDTO> getAllAttemptsForQuiz(Long quizId) {
        List<QuizAttempt> attempts = quizAttemptRepository.findByQuizId(quizId);
        return attempts.stream()
                .map(this::convertToQuizAttemptDTO)
                .collect(Collectors.toList());
    }

    @Override
    public QuizAttemptDTO gradeShortAnswer(Long answerId, Integer marks) {
        StudentAnswer answer = studentAnswerRepository.findById(answerId)
                .orElseThrow(() -> new RuntimeException("Answer not found"));

        answer.setMarksAwarded(marks);
        answer.setIsCorrect(marks > 0);
        studentAnswerRepository.save(answer);

        // Recalculate attempt score
        QuizAttempt attempt = answer.getAttempt();
        List<StudentAnswer> allAnswers = studentAnswerRepository.findByAttemptId(attempt.getId());
        int totalScore = allAnswers.stream()
                .mapToInt(a -> a.getMarksAwarded() != null ? a.getMarksAwarded() : 0)
                .sum();

        attempt.setScore(totalScore);
        QuizAttempt updatedAttempt = quizAttemptRepository.save(attempt);

        // Notify student
        notificationService.createNotification(
                attempt.getStudentId(),
                "Quiz Graded",
                "Your quiz has been graded. Score: " + totalScore + "/" + attempt.getTotalMarks(),
                Notification.NotificationType.GRADE,
                attempt.getId(),
                "QUIZ_ATTEMPT"
        );

        return convertToQuizAttemptDTO(updatedAttempt);
    }

    // ============= HELPER METHODS =============

    private Question createQuestionFromDTO(QuestionDTO dto, Quiz quiz, int orderIndex) {
        Question question = new Question();
        question.setQuiz(quiz);
        question.setQuestionText(dto.getQuestionText());
        question.setQuestionType(dto.getQuestionType());
        question.setMarks(dto.getMarks());
        question.setOrderIndex(orderIndex);
        question.setExplanation(dto.getExplanation());

        if ("MULTIPLE_CHOICE".equals(dto.getQuestionType()) && dto.getOptions() != null) {
            List<QuestionOption> options = new ArrayList<>();
            int optionIndex = 0;
            for (QuestionOptionDTO optDto : dto.getOptions()) {
                QuestionOption option = new QuestionOption();
                option.setQuestion(question);
                option.setOptionText(optDto.getOptionText());
                option.setIsCorrect(optDto.getIsCorrect() != null ? optDto.getIsCorrect() : false);
                option.setOrderIndex(optionIndex++);
                options.add(option);
            }
            question.setOptions(options);
        } else if ("TRUE_FALSE".equals(dto.getQuestionType()) || "SHORT_ANSWER".equals(dto.getQuestionType())) {
            question.setCorrectAnswer(dto.getCorrectAnswer());
        }

        return question;
    }

    private void notifyStudentsAboutNewQuiz(Quiz quiz) {
        ClassEntity classEntity = classRepository.findById(quiz.getCourseId()).orElse(null);
        if (classEntity == null) return;

        // Fetch students - handle potential lazy loading
        List<User> students = classEntity.getStudents();
        if (students == null || students.isEmpty()) return;

        for (User student : students) {
            try {
                notificationService.createNotification(
                        student.getId(),
                        "New Quiz Available",
                        "A new quiz '" + quiz.getTitle() + "' is available in " + classEntity.getName(),
                        Notification.NotificationType.ANNOUNCEMENT,
                        quiz.getId(),
                        "QUIZ"
                );
            } catch (Exception e) {
                // Log error but continue with other students
                System.err.println("Failed to notify student " + student.getId() + ": " + e.getMessage());
            }
        }
    }

    private QuizDTO convertToQuizDTO(Quiz quiz, boolean includeAnswers) {
        QuizDTO dto = new QuizDTO();
        dto.setId(quiz.getId());
        dto.setClassId(quiz.getCourseId());
        dto.setTitle(quiz.getTitle());
        dto.setDescription(quiz.getDescription());
        dto.setDuration(quiz.getTimeLimit());
        dto.setTotalMarks(quiz.getMaxGrade());
        dto.setPassingMarks(quiz.getPassingMarks());
        dto.setStartTime(quiz.getStartTime());
        dto.setEndTime(quiz.getDueDate());
        dto.setMaxAttempts(quiz.getMaxAttempts());
        dto.setRandomizeQuestions(quiz.getShuffleQuestions());
        dto.setShowResultsImmediately(quiz.getShowResults());
        dto.setStatus(quiz.getStatus());
        dto.setCreatedAt(quiz.getCreatedAt());

        if (quiz.getQuestions() != null) {
            dto.setQuestionCount(quiz.getQuestions().size());
            List<QuestionDTO> questionDTOs = new ArrayList<>();
            for (Question q : quiz.getQuestions()) {
                questionDTOs.add(convertToQuestionDTO(q, includeAnswers));
            }
            dto.setQuestions(questionDTOs);
        }

        return dto;
    }

    private QuestionDTO convertToQuestionDTO(Question question, boolean includeAnswers) {
        QuestionDTO dto = new QuestionDTO();
        dto.setId(question.getId());
        dto.setQuestionText(question.getQuestionText());
        dto.setQuestionType(question.getQuestionType());
        dto.setMarks(question.getMarks());
        dto.setExplanation(includeAnswers ? question.getExplanation() : null);

        if ("MULTIPLE_CHOICE".equals(question.getQuestionType()) && question.getOptions() != null) {
            dto.setOptions(question.getOptions().stream()
                    .map(opt -> {
                        QuestionOptionDTO optDto = new QuestionOptionDTO();
                        optDto.setId(opt.getId());
                        optDto.setOptionText(opt.getOptionText());
                        optDto.setIsCorrect(includeAnswers ? opt.getIsCorrect() : null);
                        return optDto;
                    })
                    .collect(Collectors.toList()));
        } else if (includeAnswers) {
            dto.setCorrectAnswer(question.getCorrectAnswer());
        }

        return dto;
    }

    private QuizAttemptDTO convertToQuizAttemptDTO(QuizAttempt attempt) {
        QuizAttemptDTO dto = new QuizAttemptDTO();
        dto.setId(attempt.getId());
        dto.setQuizId(attempt.getQuizId());
        dto.setStudentId(attempt.getStudentId());
        dto.setAttemptNumber(attempt.getAttemptNumber());
        dto.setStartedAt(attempt.getStartedAt());
        dto.setSubmittedAt(attempt.getSubmittedAt());
        dto.setScore(attempt.getScore());
        dto.setTotalMarks(attempt.getTotalMarks());
        dto.setStatus(attempt.getStatus());

        // Fetch and set student name
        try {
            Optional<User> student = userRepository.findById(attempt.getStudentId());
            student.ifPresent(user -> dto.setStudentName(user.getName()));
        } catch (Exception e) {
            // If student not found, just leave name as null
            System.err.println("Could not fetch student name for ID: " + attempt.getStudentId());
        }

        // Include answers if submitted
        if ("SUBMITTED".equals(attempt.getStatus()) && attempt.getAnswers() != null) {
            dto.setAnswers(attempt.getAnswers().stream()
                    .map(this::convertToStudentAnswerDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    private StudentAnswerDTO convertToStudentAnswerDTO(StudentAnswer answer) {
        StudentAnswerDTO dto = new StudentAnswerDTO();
        dto.setId(answer.getId());
        dto.setQuestionId(answer.getQuestionId());
        dto.setAnswerText(answer.getAnswerText());
        dto.setSelectedOptionId(answer.getSelectedOptionId());
        dto.setIsCorrect(answer.getIsCorrect());
        dto.setMarksAwarded(answer.getMarksAwarded());
        return dto;
    }
}
