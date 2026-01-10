package com.elearnhub.teacher_service.Controller;

import com.elearnhub.teacher_service.service.EmailVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = {"http://localhost:8081", "http://localhost:5173"}, allowCredentials = "true")
public class EmailVerificationController {
    
    @Autowired
    private EmailVerificationService emailVerificationService;
    
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        try {
            boolean verified = emailVerificationService.verifyEmail(token);
            
            Map<String, Object> response = new HashMap<>();
            if (verified) {
                response.put("success", true);
                response.put("message", "Email verified successfully! You can now log in.");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Email verification failed.");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("success", "false");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerificationEmail(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Email is required");
                return ResponseEntity.badRequest().body(error);
            }
            
            emailVerificationService.resendVerificationEmail(email);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Verification email sent successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/check-verification-status")
    public ResponseEntity<?> checkVerificationStatus(@RequestParam String email) {
        try {
            boolean verified = emailVerificationService.isEmailVerified(email);
            boolean required = emailVerificationService.isEmailVerificationRequired();
            
            Map<String, Object> response = new HashMap<>();
            response.put("verified", verified);
            response.put("required", required);
            response.put("canLogin", !required || verified);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/verification-required")
    public ResponseEntity<?> isVerificationRequired() {
        Map<String, Boolean> response = new HashMap<>();
        response.put("required", emailVerificationService.isEmailVerificationRequired());
        return ResponseEntity.ok(response);
    }
}