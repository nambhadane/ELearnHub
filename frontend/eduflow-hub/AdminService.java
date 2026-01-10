package com.elearnhub.teacher_service.service;

import com.elearnhub.teacher_service.dto.AdminStatsDTO;
import com.elearnhub.teacher_service.dto.UserDTO;
import com.elearnhub.teacher_service.dto.CourseDTO;

import java.util.List;
import java.util.Map;

public interface AdminService {
    
    // Dashboard
    AdminStatsDTO getDashboardStats();
    
    // User Management
    List<UserDTO> getAllUsers(String role, String search);
    UserDTO getUserById(Long userId);
    UserDTO createUser(UserDTO userDTO);
    UserDTO updateUser(Long userId, UserDTO userDTO);
    void deleteUser(Long userId);
    UserDTO changeUserRole(Long userId, String newRole);
    UserDTO toggleUserStatus(Long userId);
    
    // Class Management
    List<Map<String, Object>> getAllClasses();
    Map<String, Object> getClassDetails(Long classId);
    Map<String, Object> createClass(Map<String, Object> request);
    Map<String, Object> updateClass(Long classId, Map<String, Object> request);
    void addStudentToClass(Long classId, Long studentId);
    void removeStudentFromClass(Long classId, Long studentId);
    void deleteClass(Long classId);
    
    // Course Management
    List<CourseDTO> getAllCourses();
    CourseDTO createCourse(CourseDTO courseDTO);
    CourseDTO updateCourse(Long courseId, CourseDTO courseDTO);
    void deleteCourse(Long courseId);
    
    // Analytics
    Map<String, Object> getUserAnalytics();
    Map<String, Object> getCourseAnalytics();
    Map<String, Object> getActivityAnalytics();
    
    // Reports
    List<Map<String, Object>> getTopCourses();
    List<Map<String, Object>> getRecentActivities();
    
    // Assignment Management
    List<Map<String, Object>> getAllAssignments();
    Map<String, Object> getAssignmentDetails(Long assignmentId);
    void deleteAssignment(Long assignmentId);
    
    // Quiz Management
    List<Map<String, Object>> getAllQuizzes();
    Map<String, Object> getQuizDetails(Long quizId);
    void deleteQuiz(Long quizId);
    
    // Settings Management
    Map<String, Object> getSystemSettings();
    Map<String, Object> updateSystemSettings(Map<String, Object> settings);
}
