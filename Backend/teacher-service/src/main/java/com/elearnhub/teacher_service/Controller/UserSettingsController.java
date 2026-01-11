package com.elearnhub.teacher_service.Controller;

import com.elearnhub.teacher_service.entity.User;
import com.elearnhub.teacher_service.entity.UserSettings;
import com.elearnhub.teacher_service.service.UserService;
import com.elearnhub.teacher_service.service.UserSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/settings")
public class UserSettingsController {
    
    @Autowired
    private UserSettingsService settingsService;
    
    @Autowired
    private UserService userService;
    
    // Get user settings
    @GetMapping
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT') or hasRole('ADMIN')")
    public ResponseEntity<UserSettings> getSettings(Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            UserSettings settings = settingsService.getUserSettings(user.getId());
            return ResponseEntity.ok(settings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Update user settings
    @PutMapping
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT') or hasRole('ADMIN')")
    public ResponseEntity<?> updateSettings(
            @RequestBody Map<String, Object> updates,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            UserSettings settings = settingsService.updateSettings(user.getId(), updates);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Settings updated successfully");
            response.put("settings", settings);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to update settings: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // Reset settings to defaults
    @PostMapping("/reset")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT') or hasRole('ADMIN')")
    public ResponseEntity<?> resetSettings(Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            UserSettings settings = settingsService.resetToDefaults(user.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Settings reset to defaults");
            response.put("settings", settings);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to reset settings: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
