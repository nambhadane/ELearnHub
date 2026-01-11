package com.elearnhub.teacher_service.service;

import com.elearnhub.teacher_service.entity.User;
import com.elearnhub.teacher_service.entity.SystemSettings;
import com.elearnhub.teacher_service.repository.SystemSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Simple Email Service Implementation
 * This is a fallback implementation that logs emails instead of sending them
 * Use this when JavaMail dependencies are not available
 */
@Service
public class SimpleEmailServiceImpl implements EmailService {
    
    @Autowired
    private SystemSettingsRepository systemSettingsRepository;
    
    @Value("${app.base-url:http://localhost:5173}")
    private String baseUrl;
    
    // User Registration & Verification
    @Override
    public void sendVerificationEmail(User user, String verificationToken) {
        if (!isEmailServiceEnabled()) return;
        
        String verificationUrl = baseUrl + "/verify-email?token=" + verificationToken;
        
        System.out.println("📧 EMAIL: Verification Email");
        System.out.println("To: " + user.getEmail());
        System.out.println("Subject: Verify Your Email - " + getPlatformName());
        System.out.println("Content: Please verify your email by clicking: " + verificationUrl);
        System.out.println("---");
    }
    
    @Override
    public void sendWelcomeEmail(User user) {
        if (!isEmailServiceEnabled()) return;
        
        System.out.println("📧 EMAIL: Welcome Email");
        System.out.println("To: " + user.getEmail());
        System.out.println("Subject: Welcome to " + getPlatformName() + "!");
        System.out.println("Content: Welcome " + user.getName() + "! Your account is ready.");
        System.out.println("---");
    }
    
    @Override
    public void sendPasswordResetEmail(User user, String resetToken) {
        if (!isEmailServiceEnabled()) return;
        
        String resetUrl = baseUrl + "/reset-password?token=" + resetToken;
        
        System.out.println("📧 EMAIL: Password Reset");
        System.out.println("To: " + user.getEmail());
        System.out.println("Subject: Password Reset Request - " + getPlatformName());
        System.out.println("Content: Reset your password: " + resetUrl);
        System.out.println("---");
    }
    
    // Academic Notifications
    @Override
    public void sendAssignmentNotification(User student, String assignmentTitle, String courseName, String dueDate) {
        if (!isEmailServiceEnabled()) return;
        
        System.out.println("📧 EMAIL: Assignment Notification");
        System.out.println("To: " + student.getEmail());
        System.out.println("Subject: New Assignment: " + assignmentTitle);
        System.out.println("Content: New assignment '" + assignmentTitle + "' in " + courseName + " due " + dueDate);
        System.out.println("---");
    }
    
    @Override
    public void sendQuizNotification(User student, String quizTitle, String courseName, String dueDate) {
        if (!isEmailServiceEnabled()) return;
        
        System.out.println("📧 EMAIL: Quiz Notification");
        System.out.println("To: " + student.getEmail());
        System.out.println("Subject: New Quiz Available: " + quizTitle);
        System.out.println("Content: New quiz '" + quizTitle + "' in " + courseName + " due " + dueDate);
        System.out.println("---");
    }
    
    @Override
    public void sendGradeNotification(User student, String itemTitle, String grade, String courseName) {
        if (!isEmailServiceEnabled()) return;
        
        System.out.println("📧 EMAIL: Grade Notification");
        System.out.println("To: " + student.getEmail());
        System.out.println("Subject: Grade Posted: " + itemTitle);
        System.out.println("Content: Grade for '" + itemTitle + "' in " + courseName + ": " + grade);
        System.out.println("---");
    }
    
    @Override
    public void sendSubmissionConfirmation(User student, String itemTitle, String courseName) {
        if (!isEmailServiceEnabled()) return;
        
        System.out.println("📧 EMAIL: Submission Confirmation");
        System.out.println("To: " + student.getEmail());
        System.out.println("Subject: Submission Confirmed: " + itemTitle);
        System.out.println("Content: Your submission for '" + itemTitle + "' in " + courseName + " was received");
        System.out.println("---");
    }
    
    // Class & Course Notifications
    @Override
    public void sendClassEnrollmentNotification(User student, String className, String teacherName) {
        if (!isEmailServiceEnabled()) return;
        
        System.out.println("📧 EMAIL: Class Enrollment");
        System.out.println("To: " + student.getEmail());
        System.out.println("Subject: Enrolled in Class: " + className);
        System.out.println("Content: You've been enrolled in " + className + " with " + teacherName);
        System.out.println("---");
    }
    
    @Override
    public void sendLiveClassNotification(User student, String className, String startTime, String joinUrl) {
        if (!isEmailServiceEnabled()) return;
        
        System.out.println("📧 EMAIL: Live Class Notification");
        System.out.println("To: " + student.getEmail());
        System.out.println("Subject: Live Class Starting Soon: " + className);
        System.out.println("Content: " + className + " starts at " + startTime + ". Join: " + joinUrl);
        System.out.println("---");
    }
    
    @Override
    public void sendClassCancellationNotification(User student, String className, String reason) {
        if (!isEmailServiceEnabled()) return;
        
        System.out.println("📧 EMAIL: Class Cancellation");
        System.out.println("To: " + student.getEmail());
        System.out.println("Subject: Class Cancelled: " + className);
        System.out.println("Content: " + className + " has been cancelled. Reason: " + reason);
        System.out.println("---");
    }
    
    // Administrative Notifications
    @Override
    public void sendAccountStatusNotification(User user, String status, String reason) {
        if (!isEmailServiceEnabled()) return;
        
        System.out.println("📧 EMAIL: Account Status Update");
        System.out.println("To: " + user.getEmail());
        System.out.println("Subject: Account Status Update - " + getPlatformName());
        System.out.println("Content: Account status: " + status + ". Reason: " + reason);
        System.out.println("---");
    }
    
    @Override
    public void sendSystemMaintenanceNotification(User user, String maintenanceDate, String duration) {
        if (!isEmailServiceEnabled()) return;
        
        System.out.println("📧 EMAIL: System Maintenance");
        System.out.println("To: " + user.getEmail());
        System.out.println("Subject: Scheduled Maintenance - " + getPlatformName());
        System.out.println("Content: Maintenance on " + maintenanceDate + " for " + duration);
        System.out.println("---");
    }
    
    @Override
    public void sendPlatformUpdateNotification(User user, String updateDetails) {
        if (!isEmailServiceEnabled()) return;
        
        System.out.println("📧 EMAIL: Platform Update");
        System.out.println("To: " + user.getEmail());
        System.out.println("Subject: Platform Update - " + getPlatformName());
        System.out.println("Content: " + updateDetails);
        System.out.println("---");
    }
    
    // Teacher Notifications
    @Override
    public void sendNewStudentEnrollmentNotification(User teacher, String studentName, String className) {
        if (!isEmailServiceEnabled()) return;
        
        System.out.println("📧 EMAIL: New Student Enrollment");
        System.out.println("To: " + teacher.getEmail());
        System.out.println("Subject: New Student Enrollment: " + className);
        System.out.println("Content: " + studentName + " enrolled in " + className);
        System.out.println("---");
    }
    
    @Override
    public void sendSubmissionReceivedNotification(User teacher, String studentName, String assignmentTitle) {
        if (!isEmailServiceEnabled()) return;
        
        System.out.println("📧 EMAIL: Submission Received");
        System.out.println("To: " + teacher.getEmail());
        System.out.println("Subject: New Submission: " + assignmentTitle);
        System.out.println("Content: " + studentName + " submitted " + assignmentTitle);
        System.out.println("---");
    }
    
    @Override
    public void sendQuizCompletedNotification(User teacher, String studentName, String quizTitle, String score) {
        if (!isEmailServiceEnabled()) return;
        
        System.out.println("📧 EMAIL: Quiz Completed");
        System.out.println("To: " + teacher.getEmail());
        System.out.println("Subject: Quiz Completed: " + quizTitle);
        System.out.println("Content: " + studentName + " completed " + quizTitle + " - Score: " + score);
        System.out.println("---");
    }
    
    // Admin Notifications
    @Override
    public void sendNewUserRegistrationNotification(String adminEmail, User newUser) {
        if (!isEmailServiceEnabled()) return;
        
        System.out.println("📧 EMAIL: New User Registration");
        System.out.println("To: " + adminEmail);
        System.out.println("Subject: New User Registration - " + getPlatformName());
        System.out.println("Content: " + newUser.getName() + " (" + newUser.getRole() + ") registered");
        System.out.println("---");
    }
    
    @Override
    public void sendSystemErrorNotification(String adminEmail, String errorDetails) {
        if (!isEmailServiceEnabled()) return;
        
        System.out.println("📧 EMAIL: System Error Alert");
        System.out.println("To: " + adminEmail);
        System.out.println("Subject: System Error Alert - " + getPlatformName());
        System.out.println("Content: " + errorDetails);
        System.out.println("---");
    }
    
    @Override
    public void sendDailyReportEmail(String adminEmail, String reportData) {
        if (!isEmailServiceEnabled()) return;
        
        System.out.println("📧 EMAIL: Daily Report");
        System.out.println("To: " + adminEmail);
        System.out.println("Subject: Daily Report - " + getPlatformName());
        System.out.println("Content: " + reportData);
        System.out.println("---");
    }
    
    // Generic Email Methods
    @Override
    public void sendCustomEmail(String to, String subject, String content) {
        if (!isEmailServiceEnabled()) return;
        
        System.out.println("📧 EMAIL: Custom Email");
        System.out.println("To: " + to);
        System.out.println("Subject: " + subject);
        System.out.println("Content: " + content);
        System.out.println("---");
    }
    
    @Override
    public void sendBulkEmail(List<String> recipients, String subject, String content) {
        if (!isEmailServiceEnabled()) return;
        
        System.out.println("📧 EMAIL: Bulk Email");
        System.out.println("To: " + String.join(", ", recipients));
        System.out.println("Subject: " + subject);
        System.out.println("Content: " + content);
        System.out.println("---");
    }
    
    // Email Template Generation
    @Override
    public String generateEmailTemplate(String templateName, Map<String, String> variables) {
        StringBuilder template = new StringBuilder();
        template.append("Email Template: ").append(templateName).append("\n");
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            template.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        return template.toString();
    }
    
    // Utility Methods
    @Override
    public boolean isEmailServiceEnabled() {
        try {
            Optional<SystemSettings> settingsOpt = systemSettingsRepository.findCurrentSettings();
            return settingsOpt.map(SystemSettings::getEmailNotificationsEnabled).orElse(true);
        } catch (Exception e) {
            return true; // Default to enabled if settings can't be retrieved
        }
    }
    
    @Override
    public void testEmailConfiguration() {
        System.out.println("📧 EMAIL: Configuration Test");
        System.out.println("Email service is using Simple Implementation (Console Output)");
        System.out.println("To enable real emails, configure JavaMail dependencies and SMTP settings");
        System.out.println("---");
    }
    
    // Private Helper Methods
    private String getPlatformName() {
        try {
            Optional<SystemSettings> settingsOpt = systemSettingsRepository.findCurrentSettings();
            return settingsOpt.map(SystemSettings::getPlatformName).orElse("EduFlow Hub");
        } catch (Exception e) {
            return "EduFlow Hub";
        }
    }
    
    private String getSupportEmail() {
        try {
            Optional<SystemSettings> settingsOpt = systemSettingsRepository.findCurrentSettings();
            return settingsOpt.map(SystemSettings::getSupportEmail).orElse("support@eduflow.com");
        } catch (Exception e) {
            return "support@eduflow.com";
        }
    }
}