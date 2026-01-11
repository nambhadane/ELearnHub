package com.elearnhub.teacher_service.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_settings")
public class SystemSettings {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // General Settings
    @Column(nullable = false)
    private String platformName = "EduFlow Hub";
    
    @Column(columnDefinition = "TEXT")
    private String platformDescription = "Comprehensive Learning Management System";
    
    @Column(nullable = false)
    private String supportEmail = "support@eduflow.com";
    
    @Column(nullable = false)
    private Integer maxFileUploadSize = 50; // MB
    
    @Column(nullable = false)
    private Integer sessionTimeout = 30; // minutes
    
    // User Management
    @Column(nullable = false)
    private Boolean allowSelfRegistration = true;
    
    @Column(nullable = false)
    private Boolean requireEmailVerification = false;
    
    @Column(nullable = false)
    private String defaultUserRole = "STUDENT";
    
    @Column(nullable = false)
    private Integer passwordMinLength = 8;
    
    @Column(nullable = false)
    private Boolean passwordRequireSpecialChars = true;
    
    // Notifications
    @Column(nullable = false)
    private Boolean emailNotificationsEnabled = true;
    
    @Column(nullable = false)
    private Boolean pushNotificationsEnabled = false;
    
    @Column(nullable = false)
    private Integer notificationRetentionDays = 30;
    
    // Security
    @Column(nullable = false)
    private Boolean enableTwoFactorAuth = false;
    
    @Column(nullable = false)
    private Integer maxLoginAttempts = 5;
    
    @Column(nullable = false)
    private Integer lockoutDurationMinutes = 15;
    
    // Academic Settings
    @Column(nullable = false)
    private String defaultGradingScale = "PERCENTAGE";
    
    @Column(nullable = false)
    private Boolean allowLateSubmissions = true;
    
    @Column(nullable = false)
    private Integer defaultLatePenalty = 10; // percentage
    
    @Column(nullable = false)
    private LocalDate academicYearStart;
    
    @Column(nullable = false)
    private LocalDate academicYearEnd;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime updatedAt;
    
    // Constructors
    public SystemSettings() {
        this.createdAt = LocalDateTime.now();
        this.academicYearStart = LocalDate.of(2024, 9, 1);
        this.academicYearEnd = LocalDate.of(2025, 6, 30);
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getPlatformName() {
        return platformName;
    }
    
    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }
    
    public String getPlatformDescription() {
        return platformDescription;
    }
    
    public void setPlatformDescription(String platformDescription) {
        this.platformDescription = platformDescription;
    }
    
    public String getSupportEmail() {
        return supportEmail;
    }
    
    public void setSupportEmail(String supportEmail) {
        this.supportEmail = supportEmail;
    }
    
    public Integer getMaxFileUploadSize() {
        return maxFileUploadSize;
    }
    
    public void setMaxFileUploadSize(Integer maxFileUploadSize) {
        this.maxFileUploadSize = maxFileUploadSize;
    }
    
    public Integer getSessionTimeout() {
        return sessionTimeout;
    }
    
    public void setSessionTimeout(Integer sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }
    
    public Boolean getAllowSelfRegistration() {
        return allowSelfRegistration;
    }
    
    public void setAllowSelfRegistration(Boolean allowSelfRegistration) {
        this.allowSelfRegistration = allowSelfRegistration;
    }
    
    public Boolean getRequireEmailVerification() {
        return requireEmailVerification;
    }
    
    public void setRequireEmailVerification(Boolean requireEmailVerification) {
        this.requireEmailVerification = requireEmailVerification;
    }
    
    public String getDefaultUserRole() {
        return defaultUserRole;
    }
    
    public void setDefaultUserRole(String defaultUserRole) {
        this.defaultUserRole = defaultUserRole;
    }
    
    public Integer getPasswordMinLength() {
        return passwordMinLength;
    }
    
    public void setPasswordMinLength(Integer passwordMinLength) {
        this.passwordMinLength = passwordMinLength;
    }
    
    public Boolean getPasswordRequireSpecialChars() {
        return passwordRequireSpecialChars;
    }
    
    public void setPasswordRequireSpecialChars(Boolean passwordRequireSpecialChars) {
        this.passwordRequireSpecialChars = passwordRequireSpecialChars;
    }
    
    public Boolean getEmailNotificationsEnabled() {
        return emailNotificationsEnabled;
    }
    
    public void setEmailNotificationsEnabled(Boolean emailNotificationsEnabled) {
        this.emailNotificationsEnabled = emailNotificationsEnabled;
    }
    
    public Boolean getPushNotificationsEnabled() {
        return pushNotificationsEnabled;
    }
    
    public void setPushNotificationsEnabled(Boolean pushNotificationsEnabled) {
        this.pushNotificationsEnabled = pushNotificationsEnabled;
    }
    
    public Integer getNotificationRetentionDays() {
        return notificationRetentionDays;
    }
    
    public void setNotificationRetentionDays(Integer notificationRetentionDays) {
        this.notificationRetentionDays = notificationRetentionDays;
    }
    
    public Boolean getEnableTwoFactorAuth() {
        return enableTwoFactorAuth;
    }
    
    public void setEnableTwoFactorAuth(Boolean enableTwoFactorAuth) {
        this.enableTwoFactorAuth = enableTwoFactorAuth;
    }
    
    public Integer getMaxLoginAttempts() {
        return maxLoginAttempts;
    }
    
    public void setMaxLoginAttempts(Integer maxLoginAttempts) {
        this.maxLoginAttempts = maxLoginAttempts;
    }
    
    public Integer getLockoutDurationMinutes() {
        return lockoutDurationMinutes;
    }
    
    public void setLockoutDurationMinutes(Integer lockoutDurationMinutes) {
        this.lockoutDurationMinutes = lockoutDurationMinutes;
    }
    
    public String getDefaultGradingScale() {
        return defaultGradingScale;
    }
    
    public void setDefaultGradingScale(String defaultGradingScale) {
        this.defaultGradingScale = defaultGradingScale;
    }
    
    public Boolean getAllowLateSubmissions() {
        return allowLateSubmissions;
    }
    
    public void setAllowLateSubmissions(Boolean allowLateSubmissions) {
        this.allowLateSubmissions = allowLateSubmissions;
    }
    
    public Integer getDefaultLatePenalty() {
        return defaultLatePenalty;
    }
    
    public void setDefaultLatePenalty(Integer defaultLatePenalty) {
        this.defaultLatePenalty = defaultLatePenalty;
    }
    
    public LocalDate getAcademicYearStart() {
        return academicYearStart;
    }
    
    public void setAcademicYearStart(LocalDate academicYearStart) {
        this.academicYearStart = academicYearStart;
    }
    
    public LocalDate getAcademicYearEnd() {
        return academicYearEnd;
    }
    
    public void setAcademicYearEnd(LocalDate academicYearEnd) {
        this.academicYearEnd = academicYearEnd;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}