package com.elearnhub.teacher_service.Controller;

import com.elearnhub.teacher_service.dto.*;
import com.elearnhub.teacher_service.entity.User;
import com.elearnhub.teacher_service.entity.ClassEntity;
import com.elearnhub.teacher_service.repository.ClassRepository;
import com.elearnhub.teacher_service.repository.QuizRepository;
import com.elearnhub.teacher_service.service.QuizService;
import com.elearnhub.teacher_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/quizzes")
@CrossOrigin(origins = {"http://localhost:8081", "http://localhost:5173"}, allowCredentials = "true")
public class QuizController {

    @Autowired
    private QuizService quizService;

    @Autowired
    private UserService userService;

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private QuizRepository quizRepository;

    // ============= TEACHER ENDPOINTS =============

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> createQuiz(@RequestBody QuizDTO quizDTO) {
        try {
            // ✅ DEBUG: Log the incoming quiz data
            System.out.println("🎯 Creating quiz: " + quizDTO.getTitle());
            System.out.println("🎯 Class ID: " + quizDTO.getClassId());
            System.out.println("🎯 Start Time: " + quizDTO.getStartTime());
            System.out.println("🎯 End Time: " + quizDTO.getEndTime());
            
            QuizDTO created = quizService.createQuiz(quizDTO);
            System.out.println("✅ Quiz created successfully with ID: " + created.getId());
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            System.err.println("❌ Quiz creation failed: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            error.put("debug", "Check server logs for details");
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/{quizId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> updateQuiz(@PathVariable Long quizId, @RequestBody QuizDTO quizDTO) {
        try {
            QuizDTO updated = quizService.updateQuiz(quizId, quizDTO);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/{quizId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> deleteQuiz(@PathVariable Long quizId) {
        try {
            quizService.deleteQuiz(quizId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Quiz deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/{quizId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<?> getQuiz(@PathVariable Long quizId, Authentication auth) {
        try {
            boolean isTeacher = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER"));
            QuizDTO quiz = quizService.getQuizById(quizId, isTeacher);
            return ResponseEntity.ok(quiz);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/class/{classId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> getQuizzesByClass(@PathVariable Long classId) {
        try {
            System.out.println("🎯 Getting quizzes for class ID: " + classId);
            List<QuizDTO> quizzes = quizService.getQuizzesByClass(classId);
            System.out.println("✅ Found " + quizzes.size() + " quizzes for class " + classId);
            return ResponseEntity.ok(quizzes);
        } catch (Exception e) {
            System.err.println("❌ Failed to get quizzes for class " + classId + ": " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            error.put("debug", "Check server logs for details");
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/{quizId}/publish")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> publishQuiz(@PathVariable Long quizId) {
        try {
            QuizDTO published = quizService.publishQuiz(quizId);
            return ResponseEntity.ok(published);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Question management
    @PostMapping("/{quizId}/questions")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> addQuestion(@PathVariable Long quizId, @RequestBody QuestionDTO questionDTO) {
        try {
            QuestionDTO created = quizService.addQuestion(quizId, questionDTO);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/questions/{questionId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> updateQuestion(@PathVariable Long questionId, @RequestBody QuestionDTO questionDTO) {
        try {
            QuestionDTO updated = quizService.updateQuestion(questionId, questionDTO);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/questions/{questionId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> deleteQuestion(@PathVariable Long questionId) {
        try {
            quizService.deleteQuestion(questionId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Question deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/{quizId}/attempts")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> getQuizAttempts(@PathVariable Long quizId) {
        try {
            List<QuizAttemptDTO> attempts = quizService.getAllAttemptsForQuiz(quizId);
            return ResponseEntity.ok(attempts);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/answers/{answerId}/grade")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> gradeAnswer(@PathVariable Long answerId, @RequestBody Map<String, Integer> request) {
        try {
            Integer marks = request.get("marks");
            QuizAttemptDTO attempt = quizService.gradeShortAnswer(answerId, marks);
            return ResponseEntity.ok(attempt);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ✅ DEBUG: Test endpoint to check database connection
    @GetMapping("/debug/test")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<?> debugTest() {
        try {
            Map<String, Object> debug = new HashMap<>();
            
            // Test class repository
            long classCount = classRepository.count();
            debug.put("totalClasses", classCount);
            
            // Test quiz repository
            long quizCount = quizRepository.count();
            debug.put("totalQuizzes", quizCount);
            
            // Get all classes and their course relationships
            List<ClassEntity> allClasses = classRepository.findAll();
            List<Map<String, Object>> classInfo = new ArrayList<>();
            
            for (ClassEntity cls : allClasses) {
                Map<String, Object> info = new HashMap<>();
                info.put("classId", cls.getId());
                info.put("className", cls.getName());
                info.put("courseId", cls.getCourseId());
                info.put("hasCourseObject", cls.getCourse() != null);
                if (cls.getCourse() != null) {
                    info.put("courseName", cls.getCourse().getName());
                }
                classInfo.add(info);
            }
            debug.put("allClasses", classInfo);
            
            return ResponseEntity.ok(debug);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ============= STUDENT ENDPOINTS =============

    @GetMapping("/available/class/{classId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getAvailableQuizzes(@PathVariable Long classId, Authentication auth) {
        try {
            // Extract student ID from authentication
            Long studentId = extractUserId(auth);
            List<QuizDTO> quizzes = quizService.getAvailableQuizzes(classId, studentId);
            return ResponseEntity.ok(quizzes);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/{quizId}/start")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> startQuiz(@PathVariable Long quizId, Authentication auth) {
        try {
            Long studentId = extractUserId(auth);
            QuizAttemptDTO attempt = quizService.startQuizAttempt(quizId, studentId);
            return ResponseEntity.ok(attempt);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/attempts/{attemptId}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> submitQuiz(@PathVariable Long attemptId, @RequestBody List<StudentAnswerDTO> answers) {
        try {
            QuizAttemptDTO attempt = quizService.submitQuizAttempt(attemptId, answers);
            return ResponseEntity.ok(attempt);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/attempts/{attemptId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getAttempt(@PathVariable Long attemptId, Authentication auth) {
        try {
            Long studentId = extractUserId(auth);
            QuizAttemptDTO attempt = quizService.getAttemptById(attemptId, studentId);
            return ResponseEntity.ok(attempt);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/{quizId}/my-attempts")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getMyAttempts(@PathVariable Long quizId, Authentication auth) {
        try {
            Long studentId = extractUserId(auth);
            List<QuizAttemptDTO> attempts = quizService.getStudentAttempts(quizId, studentId);
            return ResponseEntity.ok(attempts);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Helper method to extract user ID from authentication
    private Long extractUserId(Authentication auth) {
        String username = auth.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
}
