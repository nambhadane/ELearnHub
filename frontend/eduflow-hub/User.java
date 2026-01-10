package com.elearnhub.teacher_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Username cannot be null")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(unique = true)
    private String username;
    
    @NotNull(message = "Name cannot be null")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;
    
    @NotNull(message = "Email cannot be null")
    @Email(message = "Email should be valid")
    @Column(unique = true)
    private String email;
    
    @NotNull(message = "Password cannot be null")
    private String password;
    
    @NotNull(message = "Role cannot be null")
    private String role = "STUDENT";
    
    private String phoneNumber;
    private String address;
    private String profilePicture;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    // Email verification fields
    @Column(name = "email_verified")
    private Boolean emailVerified = false;
    
    @Column(name = "email_verified_at")
    private java.time.LocalDateTime emailVerifiedAt;
    
    // Default constructor
    public User() {}
    
    // Constructor with basic fields
    public User(String username, String name, String email, String password, String role) {
        this.username = username;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }
    
    // Getters and setters
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
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
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
    
    // Additional getter for Spring Data JPA compatibility
    public Boolean getActive() {
        return isActive;
    }
    
    public void setActive(Boolean active) {
        this.isActive = active;
    }
    
    // Email verification getters and setters
    public Boolean getEmailVerified() {
        return emailVerified;
    }
    
    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }
    
    public java.time.LocalDateTime getEmailVerifiedAt() {
        return emailVerifiedAt;
    }
    
    public void setEmailVerifiedAt(java.time.LocalDateTime emailVerifiedAt) {
        this.emailVerifiedAt = emailVerifiedAt;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                '}';
    }
}