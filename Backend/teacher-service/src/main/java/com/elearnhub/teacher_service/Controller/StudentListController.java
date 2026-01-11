package com.elearnhub.teacher_service.Controller;

import com.elearnhub.teacher_service.dto.ParticipantDTO;
import com.elearnhub.teacher_service.entity.User;
import com.elearnhub.teacher_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/students")  // ✅ This makes it /students (root level)
public class StudentListController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping  // ✅ This maps to GET /students
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> getAllStudents(Authentication authentication) {
        try {
            String username = authentication.getName();
            User teacher = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Teacher not found"));

            // Get all students (users with role STUDENT)
            List<ParticipantDTO> students = userService.getAllStudents();
            
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to fetch students: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}