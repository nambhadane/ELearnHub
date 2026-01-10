package com.elearnhub.teacher_service.Controller;

import com.elearnhub.teacher_service.dto.AdminStatsDTO;
import com.elearnhub.teacher_service.dto.UserDTO;
import com.elearnhub.teacher_service.dto.CourseDTO;
import com.elearnhub.teacher_service.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = {"http://localhost:8081", "http://localhost:5173"}, allowCredentials = "true")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    @Autowired
    private AdminService adminService;
    
    // Dashboard Stats
    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDTO> getDashboardStats() {
        AdminStatsDTO stats = adminService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }
    
    // User Management
    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String search) {
        List<UserDTO> users = adminService.getAllUsers(role, search);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long userId) {
        UserDTO user = adminService.getUserById(userId);
        return ResponseEntity.ok(user);
    }
    
    @PostMapping("/users")
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO) {
        UserDTO created = adminService.createUser(userDTO);
        return ResponseEntity.ok(created);
    }
    
    @PutMapping("/users/{userId}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long userId,
            @RequestBody UserDTO userDTO) {
        UserDTO updated = adminService.updateUser(userId, userDTO);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/users/{userId}/role")
    public ResponseEntity<UserDTO> changeUserRole(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request) {
        UserDTO updated = adminService.changeUserRole(userId, request.get("role"));
        return ResponseEntity.ok(updated);
    }
    
    @PutMapping("/users/{userId}/status")
    public ResponseEntity<UserDTO> toggleUserStatus(@PathVariable Long userId) {
        UserDTO updated = adminService.toggleUserStatus(userId);
        return ResponseEntity.ok(updated);
    }
    
    // Class Management
    @GetMapping("/classes")
    public ResponseEntity<List<Map<String, Object>>> getAllClasses() {
        List<Map<String, Object>> classes = adminService.getAllClasses();
        return ResponseEntity.ok(classes);
    }
    
    @GetMapping("/classes/{classId}")
    public ResponseEntity<Map<String, Object>> getClassDetails(@PathVariable Long classId) {
        Map<String, Object> classDetails = adminService.getClassDetails(classId);
        return ResponseEntity.ok(classDetails);
    }
    
    @PostMapping("/classes")
    public ResponseEntity<Map<String, Object>> createClass(@RequestBody Map<String, Object> request) {
        Map<String, Object> created = adminService.createClass(request);
        return ResponseEntity.ok(created);
    }
    
    @PutMapping("/classes/{classId}")
    public ResponseEntity<Map<String, Object>> updateClass(
            @PathVariable Long classId,
            @RequestBody Map<String, Object> request) {
        Map<String, Object> updated = adminService.updateClass(classId, request);
        return ResponseEntity.ok(updated);
    }
    
    @PostMapping("/classes/{classId}/students")
    public ResponseEntity<Void> addStudentToClass(
            @PathVariable Long classId,
            @RequestBody Map<String, Long> request) {
        adminService.addStudentToClass(classId, request.get("studentId"));
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/classes/{classId}/students/{studentId}")
    public ResponseEntity<Void> removeStudentFromClass(
            @PathVariable Long classId,
            @PathVariable Long studentId) {
        adminService.removeStudentFromClass(classId, studentId);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/classes/{classId}")
    public ResponseEntity<Void> deleteClass(@PathVariable Long classId) {
        adminService.deleteClass(classId);
        return ResponseEntity.ok().build();
    }
    
    // Course Management
    @GetMapping("/courses")
    public ResponseEntity<List<CourseDTO>> getAllCourses() {
        List<CourseDTO> courses = adminService.getAllCourses();
        return ResponseEntity.ok(courses);
    }
    
    @PostMapping("/courses")
    public ResponseEntity<CourseDTO> createCourse(@RequestBody CourseDTO courseDTO) {
        CourseDTO created = adminService.createCourse(courseDTO);
        return ResponseEntity.ok(created);
    }
    
    @PutMapping("/courses/{courseId}")
    public ResponseEntity<CourseDTO> updateCourse(
            @PathVariable Long courseId,
            @RequestBody CourseDTO courseDTO) {
        CourseDTO updated = adminService.updateCourse(courseId, courseDTO);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/courses/{courseId}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long courseId) {
        adminService.deleteCourse(courseId);
        return ResponseEntity.ok().build();
    }
    
    // Analytics
    @GetMapping("/analytics/users")
    public ResponseEntity<Map<String, Object>> getUserAnalytics() {
        Map<String, Object> analytics = adminService.getUserAnalytics();
        return ResponseEntity.ok(analytics);
    }
    
    @GetMapping("/analytics/courses")
    public ResponseEntity<Map<String, Object>> getCourseAnalytics() {
        Map<String, Object> analytics = adminService.getCourseAnalytics();
        return ResponseEntity.ok(analytics);
    }
    
    @GetMapping("/analytics/activity")
    public ResponseEntity<Map<String, Object>> getActivityAnalytics() {
        Map<String, Object> analytics = adminService.getActivityAnalytics();
        return ResponseEntity.ok(analytics);
    }
    
    // System Reports
    @GetMapping("/reports/top-courses")
    public ResponseEntity<List<Map<String, Object>>> getTopCourses() {
        List<Map<String, Object>> topCourses = adminService.getTopCourses();
        return ResponseEntity.ok(topCourses);
    }
    
    @GetMapping("/reports/recent-activities")
    public ResponseEntity<List<Map<String, Object>>> getRecentActivities() {
        List<Map<String, Object>> activities = adminService.getRecentActivities();
        return ResponseEntity.ok(activities);
    }
    
    // Assignment Management
    @GetMapping("/assignments")
    public ResponseEntity<List<Map<String, Object>>> getAllAssignments() {
        List<Map<String, Object>> assignments = adminService.getAllAssignments();
        return ResponseEntity.ok(assignments);
    }
    
    @GetMapping("/assignments/{assignmentId}")
    public ResponseEntity<Map<String, Object>> getAssignmentDetails(@PathVariable Long assignmentId) {
        Map<String, Object> assignment = adminService.getAssignmentDetails(assignmentId);
        return ResponseEntity.ok(assignment);
    }
    
    @DeleteMapping("/assignments/{assignmentId}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long assignmentId) {
        adminService.deleteAssignment(assignmentId);
        return ResponseEntity.ok().build();
    }
    
    // Quiz Management
    @GetMapping("/quizzes")
    public ResponseEntity<List<Map<String, Object>>> getAllQuizzes() {
        List<Map<String, Object>> quizzes = adminService.getAllQuizzes();
        return ResponseEntity.ok(quizzes);
    }
    
    @GetMapping("/quizzes/{quizId}")
    public ResponseEntity<Map<String, Object>> getQuizDetails(@PathVariable Long quizId) {
        Map<String, Object> quiz = adminService.getQuizDetails(quizId);
        return ResponseEntity.ok(quiz);
    }
    
    @DeleteMapping("/quizzes/{quizId}")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Long quizId) {
        adminService.deleteQuiz(quizId);
        return ResponseEntity.ok().build();
    }
    
    // Settings Management
    @GetMapping("/settings")
    public ResponseEntity<Map<String, Object>> getSystemSettings() {
        Map<String, Object> settings = adminService.getSystemSettings();
        return ResponseEntity.ok(settings);
    }
    
    @PostMapping("/settings")
    public ResponseEntity<Map<String, Object>> updateSystemSettings(@RequestBody Map<String, Object> settings) {
        Map<String, Object> updatedSettings = adminService.updateSystemSettings(settings);
        return ResponseEntity.ok(updatedSettings);
    }
}
