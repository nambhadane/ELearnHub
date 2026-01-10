package com.elearnhub.teacher_service.service;

import com.elearnhub.teacher_service.dto.AdminStatsDTO;
import com.elearnhub.teacher_service.dto.UserDTO;
import com.elearnhub.teacher_service.dto.CourseDTO;
import com.elearnhub.teacher_service.entity.User;
import com.elearnhub.teacher_service.entity.Course;
import com.elearnhub.teacher_service.repository.UserRepository;
import com.elearnhub.teacher_service.repository.CourseRepository;
import com.elearnhub.teacher_service.repository.ClassRepository;
import com.elearnhub.teacher_service.repository.AssignmentRepository;
import com.elearnhub.teacher_service.repository.QuizRepository;
import com.elearnhub.teacher_service.repository.MaterialRepository;
import com.elearnhub.teacher_service.entity.SystemSettings;
import com.elearnhub.teacher_service.repository.SystemSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminServiceImpl implements AdminService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private ClassRepository classRepository;
    
    @Autowired
    private AssignmentRepository assignmentRepository;
    
    @Autowired
    private QuizRepository quizRepository;
    
    @Autowired
    private MaterialRepository materialRepository;
    
    @Autowired
    private ClassService classService;
    
    @Autowired
    private SystemSettingsRepository systemSettingsRepository;
    
    @Override
    public AdminStatsDTO getDashboardStats() {
        AdminStatsDTO stats = new AdminStatsDTO();
        
        // User counts
        stats.setTotalUsers(userRepository.count());
        stats.setTotalStudents(userRepository.countByRole("STUDENT"));
        stats.setTotalTeachers(userRepository.countByRole("TEACHER"));
        stats.setTotalAdmins(userRepository.countByRole("ADMIN"));
        
        // Course and class counts
        stats.setTotalCourses(courseRepository.count());
        stats.setTotalClasses(classRepository.count());
        stats.setActiveClasses(classRepository.count()); // All classes are active for now
        
        // Content counts
        stats.setTotalAssignments(assignmentRepository.count());
        stats.setTotalQuizzes(quizRepository.count());
        stats.setTotalMaterials(materialRepository.count());
        
        // Default values for now
        stats.setAverageAttendance(0.0);
        stats.setActiveUsersToday(0L);
        
        return stats;
    }
    
    @Override
    public List<UserDTO> getAllUsers(String role, String search) {
        List<User> users;
        
        if (role != null && !role.isEmpty()) {
            users = userRepository.findByRole(role.toUpperCase());
        } else {
            users = userRepository.findAll();
        }
        
        // Apply search filter if provided
        if (search != null && !search.isEmpty()) {
            String searchLower = search.toLowerCase();
            users = users.stream()
                .filter(u -> u.getName().toLowerCase().contains(searchLower) ||
                           u.getEmail().toLowerCase().contains(searchLower) ||
                           u.getUsername().toLowerCase().contains(searchLower))
                .collect(Collectors.toList());
        }
        
        return users.stream()
            .map(this::convertToUserDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public UserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return convertToUserDTO(user);
    }
    
    @Override
    public UserDTO createUser(UserDTO userDTO) {
        // Check if username already exists
        if (userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setAddress(userDTO.getAddress());
        user.setRole(userDTO.getRole() != null ? userDTO.getRole().toUpperCase() : "STUDENT");
        
        // Set a default password (should be changed by user)
        // In production, you should use BCrypt encoder
        user.setPassword("$2a$10$qRXnqVRd1PXZNN3wvfV5je3LPU1eSpdgTE7tjC/w8ncNUuWbmhB4q"); // "admin123"
        
        user = userRepository.save(user);
        return convertToUserDTO(user);
    }
    
    @Override
    public UserDTO updateUser(Long userId, UserDTO userDTO) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setAddress(userDTO.getAddress());
        
        user = userRepository.save(user);
        return convertToUserDTO(user);
    }
    
    @Override
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        try {
            userRepository.deleteById(userId);
        } catch (Exception e) {
            // If foreign key constraint error
            if (e.getMessage().contains("foreign key") || e.getMessage().contains("constraint")) {
                throw new RuntimeException("Cannot delete user: User has associated data (classes, assignments, etc.). Please remove or reassign their data first.");
            }
            throw new RuntimeException("Failed to delete user: " + e.getMessage());
        }
    }
    
    @Override
    public UserDTO changeUserRole(Long userId, String newRole) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setRole(newRole.toUpperCase());
        user = userRepository.save(user);
        
        return convertToUserDTO(user);
    }
    
    @Override
    public UserDTO toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Toggle active status (you'll need to add this field to User entity)
        // For now, we'll just return the user
        user = userRepository.save(user);
        
        return convertToUserDTO(user);
    }
    
    @Override
    public List<Map<String, Object>> getAllClasses() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (var classEntity : classRepository.findAll()) {
            Map<String, Object> classData = new HashMap<>();
            classData.put("id", classEntity.getId());
            classData.put("name", classEntity.getName());
            classData.put("courseId", classEntity.getCourse() != null ? classEntity.getCourse().getId() : null);
            classData.put("courseName", classEntity.getCourse() != null ? classEntity.getCourse().getName() : null);
            classData.put("teacherId", classEntity.getTeacher() != null ? classEntity.getTeacher().getId() : null);
            classData.put("teacherName", classEntity.getTeacher() != null ? classEntity.getTeacher().getName() : null);
            
            // Safely get student count
            try {
                classData.put("studentCount", classEntity.getStudents() != null ? classEntity.getStudents().size() : 0);
            } catch (Exception e) {
                classData.put("studentCount", 0);
            }
            
            result.add(classData);
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> getClassDetails(Long classId) {
        var classEntity = classRepository.findById(classId)
            .orElseThrow(() -> new RuntimeException("Class not found"));
        
        Map<String, Object> classData = new HashMap<>();
        classData.put("id", classEntity.getId());
        classData.put("name", classEntity.getName());
        classData.put("courseId", classEntity.getCourse() != null ? classEntity.getCourse().getId() : null);
        classData.put("courseName", classEntity.getCourse() != null ? classEntity.getCourse().getName() : null);
        classData.put("teacherId", classEntity.getTeacher() != null ? classEntity.getTeacher().getId() : null);
        classData.put("teacherName", classEntity.getTeacher() != null ? classEntity.getTeacher().getName() : null);
        
        // Get students safely
        List<Map<String, Object>> students = new ArrayList<>();
        try {
            if (classEntity.getStudents() != null) {
                for (User student : classEntity.getStudents()) {
                    Map<String, Object> studentData = new HashMap<>();
                    studentData.put("id", student.getId());
                    studentData.put("name", student.getName());
                    studentData.put("email", student.getEmail());
                    studentData.put("username", student.getUsername());
                    students.add(studentData);
                }
            }
        } catch (Exception e) {
            // If lazy loading fails, just return empty list
        }
        classData.put("students", students);
        
        return classData;
    }
    
    @Override
    public Map<String, Object> createClass(Map<String, Object> request) {
        String name = (String) request.get("name");
        Long teacherId = Long.valueOf(request.get("teacherId").toString());
        Long courseId = Long.valueOf(request.get("courseId").toString());
        
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("Class name is required");
        }
        
        ClassDTO created = classService.createClass(teacherId, courseId, name);
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", created.getId());
        result.put("name", created.getName());
        result.put("teacherId", created.getTeacherId());
        result.put("teacherName", created.getTeacherName());
        result.put("courseId", created.getCourseId());
        result.put("courseName", created.getCourseName());
        result.put("studentCount", created.getStudentCount());
        
        return result;
    }
    
    @Override
    public Map<String, Object> updateClass(Long classId, Map<String, Object> request) {
        String name = (String) request.get("name");
        
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("Class name is required");
        }
        
        ClassDTO updated = classService.updateClass(classId, name);
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", updated.getId());
        result.put("name", updated.getName());
        result.put("teacherId", updated.getTeacherId());
        result.put("teacherName", updated.getTeacherName());
        result.put("courseId", updated.getCourseId());
        result.put("courseName", updated.getCourseName());
        result.put("studentCount", updated.getStudentCount());
        
        return result;
    }
    
    @Override
    public void addStudentToClass(Long classId, Long studentId) {
        classService.addStudentToClass(classId, studentId);
    }
    
    @Override
    public void removeStudentFromClass(Long classId, Long studentId) {
        classService.removeStudentFromClass(classId, studentId);
    }
    
    @Override
    public void deleteClass(Long classId) {
        try {
            classService.deleteClass(classId);
        } catch (Exception e) {
            if (e.getMessage().contains("foreign key") || e.getMessage().contains("constraint")) {
                throw new RuntimeException("Cannot delete class: Class has associated data (assignments, materials, etc.). Please remove them first.");
            }
            throw new RuntimeException("Failed to delete class: " + e.getMessage());
        }
    }
    
    @Override
    public List<CourseDTO> getAllCourses() {
        return courseRepository.findAll().stream()
            .map(this::convertToCourseDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public CourseDTO createCourse(CourseDTO courseDTO) {
        Course course = new Course();
        course.setName(courseDTO.getName());
        course.setDescription(courseDTO.getDescription());
        
        // Set teacher if provided
        if (courseDTO.getTeacherId() != null) {
            User teacher = userRepository.findById(courseDTO.getTeacherId())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
            course.setTeacher(teacher);
        }
        
        course = courseRepository.save(course);
        return convertToCourseDTO(course);
    }
    
    @Override
    public CourseDTO updateCourse(Long courseId, CourseDTO courseDTO) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found"));
        
        course.setName(courseDTO.getName());
        course.setDescription(courseDTO.getDescription());
        
        if (courseDTO.getTeacherId() != null) {
            User teacher = userRepository.findById(courseDTO.getTeacherId())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
            course.setTeacher(teacher);
        }
        
        course = courseRepository.save(course);
        return convertToCourseDTO(course);
    }
    
    @Override
    public void deleteCourse(Long courseId) {
        courseRepository.deleteById(courseId);
    }
    
    @Override
    public Map<String, Object> getUserAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        
        analytics.put("totalUsers", userRepository.count());
        analytics.put("students", userRepository.countByRole("STUDENT"));
        analytics.put("teachers", userRepository.countByRole("TEACHER"));
        analytics.put("admins", userRepository.countByRole("ADMIN"));
        
        // Growth data (mock for now)
        analytics.put("monthlyGrowth", Arrays.asList(
            Map.of("month", "Jan", "users", 120),
            Map.of("month", "Feb", "users", 145),
            Map.of("month", "Mar", "users", 178),
            Map.of("month", "Apr", "users", 210),
            Map.of("month", "May", "users", 245),
            Map.of("month", "Jun", "users", 280)
        ));
        
        return analytics;
    }
    
    @Override
    public Map<String, Object> getCourseAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        
        analytics.put("totalCourses", courseRepository.count());
        analytics.put("totalClasses", classRepository.count());
        analytics.put("totalAssignments", assignmentRepository.count());
        analytics.put("totalQuizzes", quizRepository.count());
        
        return analytics;
    }
    
    @Override
    public Map<String, Object> getActivityAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        
        // Mock activity data
        analytics.put("studentEngagement", 92);
        analytics.put("teacherEngagement", 88);
        analytics.put("assignmentSubmissionRate", 85);
        analytics.put("classAttendance", 94);
        analytics.put("platformUsage", 90);
        
        return analytics;
    }
    
    @Override
    public List<Map<String, Object>> getTopCourses() {
        List<Course> courses = courseRepository.findAll();
        
        return courses.stream()
            .limit(5)
            .map(course -> {
                Map<String, Object> courseData = new HashMap<>();
                courseData.put("id", course.getId());
                courseData.put("name", course.getName());
                courseData.put("description", course.getDescription());
                courseData.put("teacherName", course.getTeacher() != null ? course.getTeacher().getName() : "N/A");
                
                // Get actual student count from classes
                try {
                    long studentCount = classRepository.findAll().stream()
                        .filter(c -> c.getCourse() != null && c.getCourse().getId().equals(course.getId()))
                        .mapToLong(c -> c.getStudents() != null ? c.getStudents().size() : 0)
                        .sum();
                    courseData.put("studentsCount", (int) studentCount);
                } catch (Exception e) {
                    courseData.put("studentsCount", 0);
                }
                
                // Calculate rating based on course popularity (students/classes ratio)
                try {
                    long classCount = classRepository.findAll().stream()
                        .filter(c -> c.getCourse() != null && c.getCourse().getId().equals(course.getId()))
                        .count();
                    int studentCount = (Integer) courseData.get("studentsCount");
                    double rating = classCount > 0 ? Math.min(5.0, 3.5 + (studentCount / (classCount * 20.0))) : 4.0;
                    courseData.put("rating", Math.round(rating * 10.0) / 10.0);
                } catch (Exception e) {
                    courseData.put("rating", 4.0);
                }
                
                return courseData;
            })
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Map<String, Object>> getRecentActivities() {
        List<Map<String, Object>> activities = new ArrayList<>();
        
        try {
            // Get recent users (last 10)
            List<User> recentUsers = userRepository.findAll().stream()
                .sorted((u1, u2) -> u2.getId().compareTo(u1.getId()))
                .limit(5)
                .collect(Collectors.toList());
            
            for (User user : recentUsers) {
                Map<String, Object> activity = new HashMap<>();
                activity.put("type", "user_registered");
                activity.put("message", "New " + user.getRole().toLowerCase() + " registered");
                activity.put("timestamp", new Date());
                activity.put("user", user.getName());
                activities.add(activity);
            }
            
            // Get recent courses (last 5)
            List<Course> recentCourses = courseRepository.findAll().stream()
                .sorted((c1, c2) -> c2.getId().compareTo(c1.getId()))
                .limit(3)
                .collect(Collectors.toList());
            
            for (Course course : recentCourses) {
                Map<String, Object> activity = new HashMap<>();
                activity.put("type", "course_created");
                activity.put("message", "New course '" + course.getName() + "' created");
                activity.put("timestamp", new Date());
                activity.put("user", course.getTeacher() != null ? course.getTeacher().getName() : "System");
                activities.add(activity);
            }
            
            // Sort by timestamp (most recent first) and limit to 8 activities
            return activities.stream()
                .sorted((a1, a2) -> ((Date) a2.get("timestamp")).compareTo((Date) a1.get("timestamp")))
                .limit(8)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            // Fallback to empty list if there's an error
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<Map<String, Object>> getAllAssignments() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (var assignment : assignmentRepository.findAll()) {
            Map<String, Object> assignmentData = new HashMap<>();
            assignmentData.put("id", assignment.getId());
            assignmentData.put("title", assignment.getTitle());
            assignmentData.put("description", assignment.getDescription());
            assignmentData.put("dueDate", assignment.getDueDate());
            assignmentData.put("maxGrade", assignment.getMaxGrade());
            assignmentData.put("courseId", assignment.getCourseId());
            assignmentData.put("status", assignment.getStatus());
            assignmentData.put("weight", assignment.getWeight());
            assignmentData.put("allowLateSubmission", assignment.getAllowLateSubmission());
            assignmentData.put("createdAt", assignment.getCreatedAt());
            assignmentData.put("type", "ASSIGNMENT"); // Add type to distinguish from quizzes
            
            // Get course name
            try {
                var course = courseRepository.findById(assignment.getCourseId());
                if (course.isPresent()) {
                    assignmentData.put("courseName", course.get().getName());
                    if (course.get().getTeacher() != null) {
                        assignmentData.put("teacherName", course.get().getTeacher().getName());
                    }
                }
            } catch (Exception e) {
                assignmentData.put("courseName", "Unknown");
            }
            
            result.add(assignmentData);
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> getAssignmentDetails(Long assignmentId) {
        var assignment = assignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new RuntimeException("Assignment not found"));
        
        Map<String, Object> assignmentData = new HashMap<>();
        assignmentData.put("id", assignment.getId());
        assignmentData.put("title", assignment.getTitle());
        assignmentData.put("description", assignment.getDescription());
        assignmentData.put("dueDate", assignment.getDueDate());
        assignmentData.put("maxGrade", assignment.getMaxGrade());
        assignmentData.put("courseId", assignment.getCourseId());
        assignmentData.put("status", assignment.getStatus());
        assignmentData.put("weight", assignment.getWeight());
        assignmentData.put("allowLateSubmission", assignment.getAllowLateSubmission());
        assignmentData.put("latePenalty", assignment.getLatePenalty());
        assignmentData.put("additionalInstructions", assignment.getAdditionalInstructions());
        assignmentData.put("createdAt", assignment.getCreatedAt());
        assignmentData.put("updatedAt", assignment.getUpdatedAt());
        
        // Get course details
        try {
            var course = courseRepository.findById(assignment.getCourseId());
            if (course.isPresent()) {
                assignmentData.put("courseName", course.get().getName());
                if (course.get().getTeacher() != null) {
                    assignmentData.put("teacherId", course.get().getTeacher().getId());
                    assignmentData.put("teacherName", course.get().getTeacher().getName());
                }
            }
        } catch (Exception e) {
            assignmentData.put("courseName", "Unknown");
        }
        
        return assignmentData;
    }
    
    @Override
    public void deleteAssignment(Long assignmentId) {
        if (!assignmentRepository.existsById(assignmentId)) {
            throw new RuntimeException("Assignment not found");
        }
        
        try {
            assignmentRepository.deleteById(assignmentId);
        } catch (Exception e) {
            if (e.getMessage().contains("foreign key") || e.getMessage().contains("constraint")) {
                throw new RuntimeException("Cannot delete assignment: Assignment has associated submissions. Please remove them first.");
            }
            throw new RuntimeException("Failed to delete assignment: " + e.getMessage());
        }
    }
    
    @Override
    public List<Map<String, Object>> getAllQuizzes() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (var quiz : quizRepository.findAll()) {
            Map<String, Object> quizData = new HashMap<>();
            quizData.put("id", quiz.getId());
            quizData.put("title", quiz.getTitle());
            quizData.put("description", quiz.getDescription());
            quizData.put("dueDate", quiz.getDueDate());
            quizData.put("maxGrade", quiz.getMaxGrade());
            quizData.put("courseId", quiz.getCourseId());
            quizData.put("status", quiz.getStatus());
            quizData.put("timeLimit", quiz.getTimeLimit());
            quizData.put("allowRetakes", quiz.getAllowRetakes());
            quizData.put("createdAt", quiz.getCreatedAt());
            quizData.put("type", "QUIZ"); // Add type to distinguish from assignments
            
            // Get course name
            try {
                var course = courseRepository.findById(quiz.getCourseId());
                if (course.isPresent()) {
                    quizData.put("courseName", course.get().getName());
                    if (course.get().getTeacher() != null) {
                        quizData.put("teacherName", course.get().getTeacher().getName());
                    }
                }
            } catch (Exception e) {
                quizData.put("courseName", "Unknown");
            }
            
            result.add(quizData);
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> getQuizDetails(Long quizId) {
        var quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> new RuntimeException("Quiz not found"));
        
        Map<String, Object> quizData = new HashMap<>();
        quizData.put("id", quiz.getId());
        quizData.put("title", quiz.getTitle());
        quizData.put("description", quiz.getDescription());
        quizData.put("dueDate", quiz.getDueDate());
        quizData.put("maxGrade", quiz.getMaxGrade());
        quizData.put("courseId", quiz.getCourseId());
        quizData.put("status", quiz.getStatus());
        quizData.put("timeLimit", quiz.getTimeLimit());
        quizData.put("allowRetakes", quiz.getAllowRetakes());
        quizData.put("maxAttempts", quiz.getMaxAttempts());
        quizData.put("showResults", quiz.getShowResults());
        quizData.put("shuffleQuestions", quiz.getShuffleQuestions());
        quizData.put("createdAt", quiz.getCreatedAt());
        quizData.put("updatedAt", quiz.getUpdatedAt());
        quizData.put("type", "QUIZ");
        
        // Get course details
        try {
            var course = courseRepository.findById(quiz.getCourseId());
            if (course.isPresent()) {
                quizData.put("courseName", course.get().getName());
                if (course.get().getTeacher() != null) {
                    quizData.put("teacherId", course.get().getTeacher().getId());
                    quizData.put("teacherName", course.get().getTeacher().getName());
                }
            }
        } catch (Exception e) {
            quizData.put("courseName", "Unknown");
        }
        
        return quizData;
    }
    
    @Override
    public void deleteQuiz(Long quizId) {
        if (!quizRepository.existsById(quizId)) {
            throw new RuntimeException("Quiz not found");
        }
        
        try {
            quizRepository.deleteById(quizId);
        } catch (Exception e) {
            if (e.getMessage().contains("foreign key") || e.getMessage().contains("constraint")) {
                throw new RuntimeException("Cannot delete quiz: Quiz has associated attempts. Please remove them first.");
            }
            throw new RuntimeException("Failed to delete quiz: " + e.getMessage());
        }
    }
    
    // Helper methods
    private UserDTO convertToUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setAddress(user.getAddress());
        dto.setRole(user.getRole());
        dto.setProfilePicture(user.getProfilePicture());
        dto.setIsActive(true); // Default for now
        
        return dto;
    }
    
    private CourseDTO convertToCourseDTO(Course course) {
        CourseDTO dto = new CourseDTO();
        dto.setId(course.getId());
        dto.setName(course.getName());
        dto.setDescription(course.getDescription());
        
        if (course.getTeacher() != null) {
            dto.setTeacherId(course.getTeacher().getId());
            dto.setTeacherName(course.getTeacher().getName());
        }
        
        return dto;
    }
    
    // Settings Management
    @Override
    public Map<String, Object> getSystemSettings() {
        Optional<SystemSettings> settingsOpt = systemSettingsRepository.findCurrentSettings();
        
        if (settingsOpt.isPresent()) {
            SystemSettings settings = settingsOpt.get();
            return convertSettingsToMap(settings);
        } else {
            // Return default settings if none exist
            SystemSettings defaultSettings = new SystemSettings();
            return convertSettingsToMap(defaultSettings);
        }
    }
    
    @Override
    public Map<String, Object> updateSystemSettings(Map<String, Object> settingsMap) {
        SystemSettings settings;
        
        // Get existing settings or create new ones
        Optional<SystemSettings> existingOpt = systemSettingsRepository.findCurrentSettings();
        if (existingOpt.isPresent()) {
            settings = existingOpt.get();
        } else {
            settings = new SystemSettings();
        }
        
        // Update settings from map
        updateSettingsFromMap(settings, settingsMap);
        
        // Save settings
        SystemSettings savedSettings = systemSettingsRepository.save(settings);
        
        return convertSettingsToMap(savedSettings);
    }
    
    private Map<String, Object> convertSettingsToMap(SystemSettings settings) {
        Map<String, Object> map = new HashMap<>();
        
        // General Settings
        map.put("platformName", settings.getPlatformName());
        map.put("platformDescription", settings.getPlatformDescription());
        map.put("supportEmail", settings.getSupportEmail());
        map.put("maxFileUploadSize", settings.getMaxFileUploadSize());
        map.put("sessionTimeout", settings.getSessionTimeout());
        
        // User Management
        map.put("allowSelfRegistration", settings.getAllowSelfRegistration());
        map.put("requireEmailVerification", settings.getRequireEmailVerification());
        map.put("defaultUserRole", settings.getDefaultUserRole());
        map.put("passwordMinLength", settings.getPasswordMinLength());
        map.put("passwordRequireSpecialChars", settings.getPasswordRequireSpecialChars());
        
        // Notifications
        map.put("emailNotificationsEnabled", settings.getEmailNotificationsEnabled());
        map.put("pushNotificationsEnabled", settings.getPushNotificationsEnabled());
        map.put("notificationRetentionDays", settings.getNotificationRetentionDays());
        
        // Security
        map.put("enableTwoFactorAuth", settings.getEnableTwoFactorAuth());
        map.put("maxLoginAttempts", settings.getMaxLoginAttempts());
        map.put("lockoutDurationMinutes", settings.getLockoutDurationMinutes());
        
        // Academic Settings
        map.put("defaultGradingScale", settings.getDefaultGradingScale());
        map.put("allowLateSubmissions", settings.getAllowLateSubmissions());
        map.put("defaultLatePenalty", settings.getDefaultLatePenalty());
        map.put("academicYearStart", settings.getAcademicYearStart().toString());
        map.put("academicYearEnd", settings.getAcademicYearEnd().toString());
        
        return map;
    }
    
    private void updateSettingsFromMap(SystemSettings settings, Map<String, Object> map) {
        // General Settings
        if (map.containsKey("platformName")) {
            settings.setPlatformName((String) map.get("platformName"));
        }
        if (map.containsKey("platformDescription")) {
            settings.setPlatformDescription((String) map.get("platformDescription"));
        }
        if (map.containsKey("supportEmail")) {
            settings.setSupportEmail((String) map.get("supportEmail"));
        }
        if (map.containsKey("maxFileUploadSize")) {
            settings.setMaxFileUploadSize((Integer) map.get("maxFileUploadSize"));
        }
        if (map.containsKey("sessionTimeout")) {
            settings.setSessionTimeout((Integer) map.get("sessionTimeout"));
        }
        
        // User Management
        if (map.containsKey("allowSelfRegistration")) {
            settings.setAllowSelfRegistration((Boolean) map.get("allowSelfRegistration"));
        }
        if (map.containsKey("requireEmailVerification")) {
            settings.setRequireEmailVerification((Boolean) map.get("requireEmailVerification"));
        }
        if (map.containsKey("defaultUserRole")) {
            settings.setDefaultUserRole((String) map.get("defaultUserRole"));
        }
        if (map.containsKey("passwordMinLength")) {
            settings.setPasswordMinLength((Integer) map.get("passwordMinLength"));
        }
        if (map.containsKey("passwordRequireSpecialChars")) {
            settings.setPasswordRequireSpecialChars((Boolean) map.get("passwordRequireSpecialChars"));
        }
        
        // Notifications
        if (map.containsKey("emailNotificationsEnabled")) {
            settings.setEmailNotificationsEnabled((Boolean) map.get("emailNotificationsEnabled"));
        }
        if (map.containsKey("pushNotificationsEnabled")) {
            settings.setPushNotificationsEnabled((Boolean) map.get("pushNotificationsEnabled"));
        }
        if (map.containsKey("notificationRetentionDays")) {
            settings.setNotificationRetentionDays((Integer) map.get("notificationRetentionDays"));
        }
        
        // Security
        if (map.containsKey("enableTwoFactorAuth")) {
            settings.setEnableTwoFactorAuth((Boolean) map.get("enableTwoFactorAuth"));
        }
        if (map.containsKey("maxLoginAttempts")) {
            settings.setMaxLoginAttempts((Integer) map.get("maxLoginAttempts"));
        }
        if (map.containsKey("lockoutDurationMinutes")) {
            settings.setLockoutDurationMinutes((Integer) map.get("lockoutDurationMinutes"));
        }
        
        // Academic Settings
        if (map.containsKey("defaultGradingScale")) {
            settings.setDefaultGradingScale((String) map.get("defaultGradingScale"));
        }
        if (map.containsKey("allowLateSubmissions")) {
            settings.setAllowLateSubmissions((Boolean) map.get("allowLateSubmissions"));
        }
        if (map.containsKey("defaultLatePenalty")) {
            settings.setDefaultLatePenalty((Integer) map.get("defaultLatePenalty"));
        }
        if (map.containsKey("academicYearStart")) {
            settings.setAcademicYearStart(LocalDate.parse((String) map.get("academicYearStart")));
        }
        if (map.containsKey("academicYearEnd")) {
            settings.setAcademicYearEnd(LocalDate.parse((String) map.get("academicYearEnd")));
        }
    }
}
