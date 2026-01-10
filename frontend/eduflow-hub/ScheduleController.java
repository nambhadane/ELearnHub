package com.elearnhub.teacher_service.Controller;

import com.elearnhub.teacher_service.dto.ScheduleDTO;
import com.elearnhub.teacher_service.entity.Schedule;
import com.elearnhub.teacher_service.entity.User;
import com.elearnhub.teacher_service.service.ScheduleService;
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
@RequestMapping("/schedules")
public class ScheduleController {
    
    @Autowired
    private ScheduleService scheduleService;
    
    @Autowired
    private UserService userService;
    
    // Create schedule (Teacher only)
    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> createSchedule(@RequestBody Schedule schedule) {
        try {
            Schedule created = scheduleService.createSchedule(schedule);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to create schedule: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // Update schedule (Teacher only)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> updateSchedule(@PathVariable Long id, @RequestBody Schedule schedule) {
        try {
            Schedule updated = scheduleService.updateSchedule(id, schedule);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to update schedule: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // Delete schedule (Teacher only)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> deleteSchedule(@PathVariable Long id) {
        try {
            scheduleService.deleteSchedule(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Schedule deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to delete schedule: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // Get schedules by class
    @GetMapping("/class/{classId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<List<ScheduleDTO>> getSchedulesByClass(@PathVariable Long classId) {
        try {
            List<ScheduleDTO> schedules = scheduleService.getSchedulesByClassId(classId);
            return ResponseEntity.ok(schedules);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Get student's timetable
    @GetMapping("/my-timetable")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<ScheduleDTO>> getMyTimetable(Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            List<ScheduleDTO> schedules = scheduleService.getSchedulesByStudentId(user.getId());
            return ResponseEntity.ok(schedules);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
