package com.elearnhub.teacher_service.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_settings")
public class UserSettings {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private Long userId;
    
    // Appearance settings
    @Column(nullable = false)
    private String theme = "light"; // light, dark, system
    
    @Column(nullable = false)
    private String language = "en"; // en, es, fr, etc.
    
    // Notification settings
    @Column(nullable = false)
    private Boolean emailNotifications = true;
    
    @Column(nullable = false)
    private Boolean pushNotifications = true;
    
    @Column(nullable = false)
    private Boolean assignmentReminders = true;
    
    @Column(nullable = false)
    private Boolean gradeNotifications = true;
    
    @Column(nullable = false)
    private Boolean messageNotifications = true;
    
    // Privacy settings
    @Column(nullable = false)
    private Boolean profileVisible = true;
    
    @Column(nullable = false)
    private Boolean showEmail = false;
    
    @Column(nullable = false)
    private Boolean showPhone = false;
    
    // Display preferences
    @Column(nullable = false)
    private Integer itemsPerPage = 10;
    
    @Column(nullable = false)
    private String dateFormat = "MM/DD/YYYY"; // MM/DD/YYYY, DD/MM/YYYY, YYYY-MM-DD
    
    @Column(nullable = false)
    private String timeFormat = "12h"; // 12h, 24h
    
    // Constructors
    public UserSettings() {}
    
    public UserSettings(Long userId) {
        this.userId = userId;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getTheme() {
        return theme;
    }
    
    public void setTheme(String theme) {
        this.theme = theme;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public Boolean getEmailNotifications() {
        return emailNotifications;
    }
    
    public void setEmailNotifications(Boolean emailNotifications) {
        this.emailNotifications = emailNotifications;
    }
    
    public Boolean getPushNotifications() {
        return pushNotifications;
    }
    
    public void setPushNotifications(Boolean pushNotifications) {
        this.pushNotifications = pushNotifications;
    }
    
    public Boolean getAssignmentReminders() {
        return assignmentReminders;
    }
    
    public void setAssignmentReminders(Boolean assignmentReminders) {
        this.assignmentReminders = assignmentReminders;
    }
    
    public Boolean getGradeNotifications() {
        return gradeNotifications;
    }
    
    public void setGradeNotifications(Boolean gradeNotifications) {
        this.gradeNotifications = gradeNotifications;
    }
    
    public Boolean getMessageNotifications() {
        return messageNotifications;
    }
    
    public void setMessageNotifications(Boolean messageNotifications) {
        this.messageNotifications = messageNotifications;
    }
    
    public Boolean getProfileVisible() {
        return profileVisible;
    }
    
    public void setProfileVisible(Boolean profileVisible) {
        this.profileVisible = profileVisible;
    }
    
    public Boolean getShowEmail() {
        return showEmail;
    }
    
    public void setShowEmail(Boolean showEmail) {
        this.showEmail = showEmail;
    }
    
    public Boolean getShowPhone() {
        return showPhone;
    }
    
    public void setShowPhone(Boolean showPhone) {
        this.showPhone = showPhone;
    }
    
    public Integer getItemsPerPage() {
        return itemsPerPage;
    }
    
    public void setItemsPerPage(Integer itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }
    
    public String getDateFormat() {
        return dateFormat;
    }
    
    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }
    
    public String getTimeFormat() {
        return timeFormat;
    }
    
    public void setTimeFormat(String timeFormat) {
        this.timeFormat = timeFormat;
    }
}
