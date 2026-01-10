package com.elearnhub.teacher_service.service;

import com.elearnhub.teacher_service.entity.User;

public interface EmailService {
    
    // User Registration & Verification
    void sendVerificationEmail(User user, String verificationToken);
    void sendWelcomeEmail(User user);
    void sendPasswordResetEmail(User user, String resetToken);
    
    // Academic Notifications
    void sendAssignmentNotification(User student, String assignmentTitle, String courseName, String dueDate);
    void sendQuizNotification(User student, String quizTitle, String courseName, String dueDate);
    void sendGradeNotification(User student, String itemTitle, String grade, String courseName);
    void sendSubmissionConfirmation(User student, String itemTitle, String courseName);
    
    // Class & Course Notifications
    void sendClassEnrollmentNotification(User student, String className, String teacherName);
    void sendLiveClassNotification(User student, String className, String startTime, String joinUrl);
    void sendClassCancellationNotification(User student, String className, String reason);
    
    // Administrative Notifications
    void sendAccountStatusNotification(User user, String status, String reason);
    void sendSystemMaintenanceNotification(User user, String maintenanceDate, String duration);
    void sendPlatformUpdateNotification(User user, String updateDetails);
    
    // Teacher Notifications
    void sendNewStudentEnrollmentNotification(User teacher, String studentName, String className);
    void sendSubmissionReceivedNotification(User teacher, String studentName, String assignmentTitle);
    void sendQuizCompletedNotification(User teacher, String studentName, String quizTitle, String score);
    
    // Admin Notifications
    void sendNewUserRegistrationNotification(String adminEmail, User newUser);
    void sendSystemErrorNotification(String adminEmail, String errorDetails);
    void sendDailyReportEmail(String adminEmail, String reportData);
    
    // Generic Email
    void sendCustomEmail(String to, String subject, String content);
    void sendBulkEmail(java.util.List<String> recipients, String subject, String content);
    
    // Email Templates
    String generateEmailTemplate(String templateName, java.util.Map<String, String> variables);
    
    // Email Verification
    boolean isEmailServiceEnabled();
    void testEmailConfiguration();
}