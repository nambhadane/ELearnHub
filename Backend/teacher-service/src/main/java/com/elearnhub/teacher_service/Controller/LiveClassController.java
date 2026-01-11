package com.elearnhub.teacher_service.Controller;

import com.elearnhub.teacher_service.dto.LiveClassDTO;
import com.elearnhub.teacher_service.entity.User;
import com.elearnhub.teacher_service.service.LiveClassService;
import com.elearnhub.teacher_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/live-classes")
public class LiveClassController {

    @Autowired
    private LiveClassService liveClassService;

    @Autowired
    private UserService userService;

    // ============= TEACHER ENDPOINTS =============

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> scheduleLiveClass(@RequestBody LiveClassDTO liveClassDTO, Authentication auth) {
        try {
            Long teacherId = extractUserId(auth);
            liveClassDTO.setHostId(teacherId);
            LiveClassDTO created = liveClassService.scheduleLiveClass(liveClassDTO);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> updateLiveClass(@PathVariable Long id, @RequestBody LiveClassDTO liveClassDTO) {
        try {
            LiveClassDTO updated = liveClassService.updateLiveClass(id, liveClassDTO);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> cancelLiveClass(@PathVariable Long id) {
        try {
            liveClassService.cancelLiveClass(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Live class cancelled successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> startLiveClass(@PathVariable Long id, Authentication auth) {
        try {
            Long teacherId = extractUserId(auth);
            LiveClassDTO started = liveClassService.startLiveClass(id, teacherId);
            return ResponseEntity.ok(started);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/{id}/end")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> endLiveClass(@PathVariable Long id, Authentication auth) {
        try {
            Long teacherId = extractUserId(auth);
            LiveClassDTO ended = liveClassService.endLiveClass(id, teacherId);
            return ResponseEntity.ok(ended);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/class/{classId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> getLiveClassesByClass(@PathVariable Long classId) {
        try {
            List<LiveClassDTO> liveClasses = liveClassService.getLiveClassesByClass(classId);
            return ResponseEntity.ok(liveClasses);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/my-classes")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> getMyLiveClasses(Authentication auth) {
        try {
            Long teacherId = extractUserId(auth);
            List<LiveClassDTO> liveClasses = liveClassService.getTeacherLiveClasses(teacherId);
            return ResponseEntity.ok(liveClasses);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ============= STUDENT ENDPOINTS =============

    @GetMapping("/available/class/{classId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getAvailableLiveClasses(@PathVariable Long classId) {
        try {
            List<LiveClassDTO> liveClasses = liveClassService.getAvailableLiveClasses(classId);
            return ResponseEntity.ok(liveClasses);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/{id}/join")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> joinLiveClass(@PathVariable Long id, Authentication auth) {
        try {
            Long studentId = extractUserId(auth);
            LiveClassDTO liveClass = liveClassService.joinLiveClass(id, studentId);
            return ResponseEntity.ok(liveClass);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ============= COMMON ENDPOINTS =============

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<?> getLiveClass(@PathVariable Long id) {
        try {
            LiveClassDTO liveClass = liveClassService.getLiveClassById(id);
            return ResponseEntity.ok(liveClass);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/meeting/{meetingId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<?> getLiveClassByMeetingId(@PathVariable String meetingId) {
        try {
            LiveClassDTO liveClass = liveClassService.getLiveClassByMeetingId(meetingId);
            return ResponseEntity.ok(liveClass);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Helper method
    private Long extractUserId(Authentication auth) {
        String username = auth.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
}
