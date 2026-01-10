// ============================================
// ClassController - Complete Implementation
// ============================================

package com.elearnhub.teacher_service.Controller;

import com.elearnhub.teacher_service.dto.ClassDTO;
import com.elearnhub.teacher_service.dto.ParticipantDTO;
import com.elearnhub.teacher_service.entity.User;
import com.elearnhub.teacher_service.service.ClassService;
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
@RequestMapping("/classes")
public class ClassController {

    @Autowired
    private ClassService classService;

    @Autowired
    private UserService userService;

    // ✅ ADD: Create class endpoint for teachers
    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> createClass(
            @RequestParam Long courseId,
            @RequestParam String name,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            User teacher = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Teacher not found"));

            // Create class
            ClassDTO createdClass = classService.createClass(teacher.getId(), courseId, name);

            return ResponseEntity.status(HttpStatus.CREATED).body(createdClass);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to create class: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get classes by teacher
    @GetMapping("/teacher/{teacherId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> getClassesByTeacher(@PathVariable Long teacherId) {
        try {
            List<ClassDTO> classes = classService.getClassesByTeacher(teacherId);
            return ResponseEntity.ok(classes);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to fetch classes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get students in a class
    @GetMapping("/{classId}/students")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> getClassStudents(
            @PathVariable Long classId,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            User teacher = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Teacher not found"));

            // Verify teacher has access to this class
            if (!classService.hasAccessToClass(classId, teacher.getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Unauthorized: You don't have access to this class");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            List<ParticipantDTO> students = classService.getClassStudents(classId);
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to fetch students: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ ADD THIS ENDPOINT - Add student to class
    @PostMapping("/{classId}/students")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> addStudentToClass(
            @PathVariable Long classId,
            @RequestParam Long studentId,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            User teacher = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Teacher not found"));

            // Verify teacher has access to this class
            if (!classService.hasAccessToClass(classId, teacher.getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Unauthorized: You don't have access to this class");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            // Add student to class
            classService.addStudentToClass(classId, studentId);

            Map<String, String> success = new HashMap<>();
            success.put("message", "Student added to class successfully");
            return ResponseEntity.ok(success);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to add student to class: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Remove student from class (optional)
    @DeleteMapping("/{classId}/students/{studentId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> removeStudentFromClass(
            @PathVariable Long classId,
            @PathVariable Long studentId,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            User teacher = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Teacher not found"));

            // Verify teacher has access to this class
            if (!classService.hasAccessToClass(classId, teacher.getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Unauthorized: You don't have access to this class");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            // Remove student from class
            classService.removeStudentFromClass(classId, studentId);

            Map<String, String> success = new HashMap<>();
            success.put("message", "Student removed from class successfully");
            return ResponseEntity.ok(success);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to remove student from class: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ STUDENT ENDPOINTS - Get classes for a student
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<?> getClassesByStudent(
            @PathVariable Long studentId,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            User currentUser = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Students can only access their own classes, teachers/admins can access any
            if (currentUser.getRole().equals("STUDENT") && !currentUser.getId().equals(studentId)) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Unauthorized: You can only access your own classes");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            List<ClassDTO> classes = classService.getClassesByStudent(studentId);
            return ResponseEntity.ok(classes);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to fetch student classes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ STUDENT ENDPOINTS - Get classes for authenticated student
    @GetMapping("/my-classes")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getMyClasses(Authentication authentication) {
        try {
            String username = authentication.getName();
            User student = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            List<ClassDTO> classes = classService.getClassesByStudent(student.getId());
            return ResponseEntity.ok(classes);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to fetch your classes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ STUDENT ENDPOINTS - Get class details (for both teachers and students)
    @GetMapping("/{classId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT') or hasRole('ADMIN')")
    public ResponseEntity<?> getClassDetails(
            @PathVariable Long classId,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            User currentUser = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Check if user has access to this class
            if (!classService.hasAccessToClass(classId, currentUser.getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Unauthorized: You don't have access to this class");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            // Get class details
            var classEntity = classService.getClassById(classId);
            if (classEntity.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Class not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            // Convert to DTO
            ClassDTO classDTO = new ClassDTO();
            classDTO.setId(classEntity.get().getId());
            classDTO.setName(classEntity.get().getName());
            
            if (classEntity.get().getTeacher() != null) {
                classDTO.setTeacherId(classEntity.get().getTeacher().getId());
                classDTO.setTeacherName(classEntity.get().getTeacher().getName());
            }
            
            if (classEntity.get().getCourse() != null) {
                classDTO.setCourseId(classEntity.get().getCourse().getId());
                classDTO.setCourseName(classEntity.get().getCourse().getName());
            }
            
            classDTO.setStudentCount(classEntity.get().getStudents() != null ? 
                classEntity.get().getStudents().size() : 0);

            return ResponseEntity.ok(classDTO);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to fetch class details: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}

