package com.elearnhub.teacher_service.Controller;

import com.elearnhub.teacher_service.entity.User;
import com.elearnhub.teacher_service.service.UserService;
import com.elearnhub.teacher_service.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = {"http://localhost:8081", "http://localhost:5173"}, allowCredentials = "true")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private com.elearnhub.teacher_service.service.EmailVerificationService emailVerificationService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        try {
            // First authenticate the user credentials
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
            
            // Get user details
            Optional<User> domainUserOpt = userService.findByUsername(loginRequest.getUsername());
            if (domainUserOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            User user = domainUserOpt.get();
            
            // ✅ CRITICAL: Check email verification if required (EXEMPT ADMINS)
            // Admins should always be able to login to manage email verification settings
            boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());
            
            // ✅ DEBUG: Add safety checks
            System.out.println("🔍 Login check - User: " + user.getUsername() + ", Role: " + user.getRole() + ", IsAdmin: " + isAdmin);
            System.out.println("🔍 User details - Name: '" + user.getName() + "', Email: '" + user.getEmail() + "', EmailVerified: " + user.getEmailVerified());
            
            boolean emailVerificationRequired = false;
            try {
                emailVerificationRequired = emailVerificationService != null && emailVerificationService.isEmailVerificationRequired();
                System.out.println("🔍 Email verification required: " + emailVerificationRequired);
            } catch (Exception e) {
                System.err.println("❌ Error checking email verification requirement: " + e.getMessage());
                // Default to false if there's an error
                emailVerificationRequired = false;
            }
            
            if (!isAdmin && emailVerificationRequired && !user.getEmailVerified()) {
                Map<String, Object> error = new HashMap<>();
                error.put("message", "Email verification required");
                error.put("emailVerificationRequired", true);
                error.put("email", user.getEmail());
                error.put("userId", user.getId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            // If verification passed or not required, proceed with login
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtUtil.generateToken((org.springframework.security.core.userdetails.User) authentication.getPrincipal());

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("role", user.getRole());
            // ✅ FIX: Create user map with null-safe values
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("username", user.getUsername() != null ? user.getUsername() : "");
            userMap.put("name", user.getName() != null ? user.getName() : "");
            userMap.put("email", user.getEmail() != null ? user.getEmail() : "");
            userMap.put("role", user.getRole() != null ? user.getRole() : "");
            userMap.put("emailVerified", user.getEmailVerified() != null ? user.getEmailVerified() : false);
            
            response.put("user", userMap);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // ✅ DEBUG: Log the actual error to help diagnose the issue
            System.err.println("❌ Login error for user: " + loginRequest.getUsername());
            System.err.println("❌ Error details: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> error = new HashMap<>();
            error.put("message", "Invalid username or password");
            error.put("debug", e.getMessage()); // ✅ Temporary debug info
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    // ✅ FIXED: Register endpoint with proper name validation
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User registerRequest) {
        try {
            // Validate required fields
            if (registerRequest.getUsername() == null || registerRequest.getUsername().trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Username is required");
                return ResponseEntity.badRequest().body(error);
            }
            
            // ✅ CRITICAL FIX: Validate name field (this was missing!)
            if (registerRequest.getName() == null || registerRequest.getName().trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Name is required");
                return ResponseEntity.badRequest().body(error);
            }
            
            if (registerRequest.getPassword() == null || registerRequest.getPassword().trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Password is required");
                return ResponseEntity.badRequest().body(error);
            }
            
            if (registerRequest.getEmail() == null || registerRequest.getEmail().trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Email is required");
                return ResponseEntity.badRequest().body(error);
            }

            // Check if username already exists
            if (userService.existsByUsername(registerRequest.getUsername())) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Username already exists");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
            }

            // ✅ Check if email already exists
            Optional<User> existingUserByEmail = userService.findByEmail(registerRequest.getEmail());
            if (existingUserByEmail.isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Email already exists");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
            }

            // Set default role to "STUDENT" if not provided (changed from TEACHER to STUDENT)
            if (registerRequest.getRole() == null || registerRequest.getRole().trim().isEmpty()) {
                registerRequest.setRole("STUDENT");
            }

            // Encode password before saving
            String originalPassword = registerRequest.getPassword();
            String encodedPassword = passwordEncoder.encode(registerRequest.getPassword());
            registerRequest.setPassword(encodedPassword);
            
            // ✅ DEBUG: Log password encoding
            System.out.println("🔐 REGISTRATION DEBUG:");
            System.out.println("🔐 Original password length: " + originalPassword.length());
            System.out.println("🔐 Encoded password: " + encodedPassword.substring(0, Math.min(30, encodedPassword.length())) + "...");
            System.out.println("🔐 Encoded password length: " + encodedPassword.length());

            // ✅ Ensure name is properly set (trim whitespace)
            registerRequest.setName(registerRequest.getName().trim());
            registerRequest.setUsername(registerRequest.getUsername().trim());
            registerRequest.setEmail(registerRequest.getEmail().trim());

            // Save user
            User savedUser = userService.createUser(registerRequest);

            // Return success response with appropriate message based on email verification requirement
            Map<String, Object> response = new HashMap<>();
            
            // Admins are exempt from email verification requirements
            boolean isAdmin = "ADMIN".equalsIgnoreCase(savedUser.getRole());
            boolean requiresVerification = !isAdmin && emailVerificationService.isEmailVerificationRequired();
            
            if (requiresVerification) {
                response.put("message", "Registration successful! Please check your email to verify your account before logging in.");
                response.put("emailVerificationRequired", true);
                response.put("emailSent", true);
            } else {
                if (isAdmin) {
                    response.put("message", "Admin registration successful! You can now log in.");
                } else {
                    response.put("message", "Registration successful! You can now log in.");
                }
                response.put("emailVerificationRequired", false);
            }
            
            // ✅ FIX: Create user map with null-safe values
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", savedUser.getId());
            userMap.put("username", savedUser.getUsername() != null ? savedUser.getUsername() : "");
            userMap.put("name", savedUser.getName() != null ? savedUser.getName() : "");
            userMap.put("email", savedUser.getEmail() != null ? savedUser.getEmail() : "");
            userMap.put("role", savedUser.getRole() != null ? savedUser.getRole() : "");
            userMap.put("emailVerified", savedUser.getEmailVerified() != null ? savedUser.getEmailVerified() : false);
            
            response.put("user", userMap);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Registration failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ DEBUG: Test password encoding endpoint
    @PostMapping("/test-password")
    public ResponseEntity<?> testPassword(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");
            
            if (username == null || password == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Username and password are required");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Find user
            Optional<User> userOpt = userService.findByUsername(username);
            if (userOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "User not found");
                return ResponseEntity.badRequest().body(error);
            }
            
            User user = userOpt.get();
            String storedPassword = user.getPassword();
            String encodedTestPassword = passwordEncoder.encode(password);
            boolean matches = passwordEncoder.matches(password, storedPassword);
            
            Map<String, Object> response = new HashMap<>();
            response.put("username", username);
            response.put("passwordProvided", password);
            response.put("storedPasswordPrefix", storedPassword.substring(0, Math.min(30, storedPassword.length())));
            response.put("storedPasswordLength", storedPassword.length());
            response.put("encodedTestPassword", encodedTestPassword.substring(0, Math.min(30, encodedTestPassword.length())));
            response.put("passwordMatches", matches);
            response.put("isBCryptFormat", storedPassword.startsWith("$2"));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Test failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ TEMPORARY: Password reset endpoint for testing
    @PostMapping("/reset-password-temp")
    public ResponseEntity<?> resetPasswordTemp(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String newPassword = request.get("newPassword");
            
            if (username == null || newPassword == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Username and newPassword are required");
                return ResponseEntity.badRequest().body(error);
            }
            
            Optional<User> userOpt = userService.findByUsername(username);
            if (userOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "User not found");
                return ResponseEntity.badRequest().body(error);
            }
            
            User user = userOpt.get();
            String encodedPassword = passwordEncoder.encode(newPassword);
            user.setPassword(encodedPassword);
            userService.updateUser(user);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Password reset successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Password reset failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }


    public static class JwtResponse {
        private String token;
        private String role;

        public JwtResponse(String token, String role) {
            this.token = token;
            this.role = role;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }
}