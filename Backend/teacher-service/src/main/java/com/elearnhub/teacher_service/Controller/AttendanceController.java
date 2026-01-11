package com.elearnhub.teacher_service.Controller;

import com.elearnhub.teacher_service.dto.AttendanceSessionDTO;
import com.elearnhub.teacher_service.dto.AttendanceRecordDTO;
import com.elearnhub.teacher_service.dto.AttendanceStatisticsDTO;
import com.elearnhub.teacher_service.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    // ============================================
    // Session Management (Teacher Only)
    // ============================================

    @PostMapping("/sessions")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> createSession(@RequestBody AttendanceSessionDTO sessionDTO, Authentication auth) {
        try {
            AttendanceSessionDTO created = attendanceService.createSession(sessionDTO);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/sessions/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> updateSession(@PathVariable Long id, @RequestBody AttendanceSessionDTO sessionDTO) {
        try {
            AttendanceSessionDTO updated = attendanceService.updateSession(id, sessionDTO);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/sessions/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> deleteSession(@PathVariable Long id) {
        try {
            attendanceService.deleteSession(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Session deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/sessions/{id}")
    public ResponseEntity<?> getSessionById(@PathVariable Long id) {
        try {
            AttendanceSessionDTO session = attendanceService.getSessionById(id);
            return ResponseEntity.ok(session);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/sessions/class/{classId}")
    public ResponseEntity<?> getSessionsByClass(@PathVariable Long classId) {
        try {
            List<AttendanceSessionDTO> sessions = attendanceService.getSessionsByClass(classId);
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/sessions/class/{classId}/range")
    public ResponseEntity<?> getSessionsByDateRange(
            @PathVariable Long classId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<AttendanceSessionDTO> sessions = attendanceService.getSessionsByClassAndDateRange(classId, startDate, endDate);
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ============================================
    // Attendance Marking (Teacher Only)
    // ============================================

    @PostMapping("/sessions/{sessionId}/mark")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> markAttendance(
            @PathVariable Long sessionId,
            @RequestParam Long studentId,
            @RequestParam String status,
            @RequestParam(required = false) String notes,
            Authentication auth) {
        try {
            // Get teacher ID from authentication
            Long teacherId = 1L; // TODO: Get from auth
            AttendanceRecordDTO record = attendanceService.markAttendance(sessionId, studentId, status, teacherId, notes);
            return ResponseEntity.ok(record);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/sessions/{sessionId}/mark-bulk")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> markBulkAttendance(
            @PathVariable Long sessionId,
            @RequestBody Map<Long, String> studentStatusMap,
            Authentication auth) {
        try {
            // Get teacher ID from authentication
            Long teacherId = 1L; // TODO: Get from auth
            List<AttendanceRecordDTO> records = attendanceService.markBulkAttendance(sessionId, studentStatusMap, teacherId);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/records/{recordId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> updateAttendanceRecord(
            @PathVariable Long recordId,
            @RequestParam String status,
            @RequestParam(required = false) String notes) {
        try {
            AttendanceRecordDTO updated = attendanceService.updateAttendanceRecord(recordId, status, notes);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ============================================
    // Attendance Records (View)
    // ============================================

    @GetMapping("/records/session/{sessionId}")
    public ResponseEntity<?> getRecordsBySession(@PathVariable Long sessionId) {
        try {
            List<AttendanceRecordDTO> records = attendanceService.getRecordsBySession(sessionId);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/records/student/{studentId}")
    public ResponseEntity<?> getRecordsByStudent(@PathVariable Long studentId) {
        try {
            List<AttendanceRecordDTO> records = attendanceService.getRecordsByStudent(studentId);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/records/class/{classId}/student/{studentId}")
    public ResponseEntity<?> getRecordsByClassAndStudent(
            @PathVariable Long classId,
            @PathVariable Long studentId) {
        try {
            List<AttendanceRecordDTO> records = attendanceService.getRecordsByClassAndStudent(classId, studentId);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ============================================
    // Statistics
    // ============================================

    @GetMapping("/statistics/student")
    public ResponseEntity<?> getStudentStatistics(
            @RequestParam Long classId,
            @RequestParam Long studentId) {
        try {
            AttendanceStatisticsDTO stats = attendanceService.getStudentStatistics(classId, studentId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/statistics/class/{classId}")
    public ResponseEntity<?> getClassStatistics(@PathVariable Long classId) {
        try {
            List<AttendanceStatisticsDTO> stats = attendanceService.getClassStatistics(classId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/statistics/session/{sessionId}")
    public ResponseEntity<?> getSessionStatistics(@PathVariable Long sessionId) {
        try {
            Map<String, Object> stats = attendanceService.getSessionStatistics(sessionId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
