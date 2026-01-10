package com.elearnhub.teacher_service.dto;

import java.time.LocalDateTime;

public class UserDTO {
    private Long id;
    private String username;
    private String name;
    private String email;
    private String phoneNumber;
    private String address;
    private String role;
    private String profilePicture;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    
    // For statistics
    private Integer classesCount;
    private Integer enrollmentsCount;
    
    // Constructors
    public UserDTO() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getProfilePicture() {
        return profilePicture;
    }
    
    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }
    
    public Integer getClassesCount() {
        return classesCount;
    }
    
    public void setClassesCount(Integer classesCount) {
        this.classesCount = classesCount;
    }
    
    public Integer getEnrollmentsCount() {
        return enrollmentsCount;
    }
    
    public void setEnrollmentsCount(Integer enrollmentsCount) {
        this.enrollmentsCount = enrollmentsCount;
    }
}
