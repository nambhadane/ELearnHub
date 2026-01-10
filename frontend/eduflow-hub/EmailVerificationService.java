package com.elearnhub.teacher_service.service;

import com.elearnhub.teacher_service.entity.User;

public interface EmailVerificationService {
    
    // Generate and send verification email
    String generateVerificationToken(User user);
    void sendVerificationEmail(User user);
    void resendVerificationEmail(String email);
    
    // Verify email
    boolean verifyEmail(String token);
    boolean isEmailVerified(Long userId);
    boolean isEmailVerified(String email);
    
    // Token management
    boolean isTokenValid(String token);
    void invalidateUserTokens(Long userId);
    void cleanupExpiredTokens();
    
    // Verification status
    void markEmailAsVerified(Long userId);
    void markEmailAsUnverified(Long userId);
    
    // Admin functions
    void forceVerifyUser(Long userId);
    void sendVerificationReminderEmail(Long userId);
    void sendWelcomeEmail(User user);
    
    // Settings integration
    boolean isEmailVerificationRequired();
    void updateVerificationRequirement(boolean required);
}