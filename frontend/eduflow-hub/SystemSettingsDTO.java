package com.elearnhub.teacher_service.dto;

import java.time.LocalDate;

public class SystemSettingsDTO {
    
    // General Settings
    private String platformName;
    private String platformDescription;
    private String supportEmail;
    private Integer maxFileUploadSize;
    private Integer sessionTimeout;
    
    // User Management
    private Boolean allowSelfRegistration;
    private Boolean requireEmailVerification;
    private String defaultUserRole;
    private Integer passwordMinLength;
    private Boolean passwordRequireSpecialChars;
    
    // Notifications
    private Boolean emailNotificationsEnabled;
    private Boolean pushNotificationsEnabled;
    private Integer notificationRetentionDays;
    
    // Security
    private Boolean enableTwoFactorAuth;
    private Integer maxLoginAttempts;
    private Integer lockoutDurationMinutes;
    
    // Academic Settings
    private String defaultGradingScale;
    private Boolean allowLateSubmissions;
    private Integer defaultLatePenalty;
    private LocalDate academicYearStart;
    private LocalDate academicYearEnd;
    
    // Constructors
    public SystemSettingsDTO() {}
    
    // Getters and Setters
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
}