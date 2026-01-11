package com.elearnhub.teacher_service.Controller;

import com.elearnhub.teacher_service.dto.ClassDTO;
import com.elearnhub.teacher_service.dto.CourseDTO;
import com.elearnhub.teacher_service.entity.Course;
import com.elearnhub.teacher_service.entity.User;
import com.elearnhub.teacher_service.repository.ClassEntityRepository;
import com.elearnhub.teacher_service.service.ClassService;
import com.elearnhub.teacher_service.service.CourseService;
import com.elearnhub.teacher_service.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/courses")
public class CourseController {

    @Autowired
    private ClassService classService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserService userService;

    @Autowired
    private ClassEntityRepository classRepository;

    // ✅ Create course - matches frontend format
    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> createCourse(@RequestBody Map<String, String> request,
                                        Authentication authentication) {
        try {
            // Get teacher from authentication
            String username = authentication.getName();
            User teacher = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

            // Frontend sends: { name, subject, description }
            String courseName = request.get("name");
            String subject = request.get("subject");
            String description = request.get("description");

            // Use name if provided, otherwise use subject
            if (courseName == null || courseName.trim().isEmpty()) {
                courseName = subject != null ? subject : "Untitled Course";
            }
            if (description == null) {
                description = "";
            }

            // Create Course entity
            Course course = new Course();
            course.setName(courseName);
            course.setDescription(description);
            course.setTeacherId(teacher.getId());
            course.setStudents(new ArrayList<>()); // Initialize empty list

            // Save course
            Course createdCourse = courseService.createCourse(course);

            // Response format matching frontend expectations
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Course created successfully");
            Map<String, Object> courseResponse = new HashMap<>();
            courseResponse.put("id", createdCourse.getId());
            courseResponse.put("name", createdCourse.getName());
            courseResponse.put("subject", createdCourse.getName()); // For frontend compatibility
            courseResponse.put("description", createdCourse.getDescription());
            courseResponse.put("teacherId", createdCourse.getTeacherId());
            courseResponse.put("students", 0); // New course has 0 students
            response.put("course", courseResponse);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to create course: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ Get all courses for authenticated teacher (not all courses in system)
    @GetMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> getCourses(Authentication authentication) {
        try {
            // Get teacher from authentication
            String username = authentication.getName();
            User teacher = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

            // Get courses for this teacher only
            List<Course> courses = courseService.getCoursesByTeacherId(teacher.getId());

            // Convert to frontend format
            List<Map<String, Object>> response = courses.stream()
                .map(this::convertCourseToResponse)
                .collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to fetch courses: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/{courseId}")
    @PreAuthorize("hasRole('TEACHER') || hasRole('STUDENT')")
    @Transactional
    public ResponseEntity<?> getCourseById(@PathVariable Long courseId,
                                         Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

            Optional<Course> courseOptional = courseService.getCourseById(courseId);
            if (courseOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Course not found with ID: " + courseId));
            }

            Course course = courseOptional.get();

            // ===========================
            //  TEACHER ACCESS VALIDATION
            // ===========================
            if (user.getRole().equals("TEACHER")) {
                if (!course.getTeacherId().equals(user.getId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Unauthorized: Course does not belong to this teacher"));
                }
            }
            // ===========================
            //  STUDENT ACCESS VALIDATION
            // ===========================
            else if (user.getRole().equals("STUDENT")) {
                boolean isEnrolled = false;
                // Option 1 → Direct enrollment in course
                if (course.getStudents() != null) {
                    course.getStudents().size(); // force lazy load
                    isEnrolled = course.getStudents().stream()
                        .anyMatch(student -> student.getId().equals(user.getId()));
                }
                // Option 2 → Enrollment through a ClassEntity (class assigned to course)
                if (!isEnrolled) {
                    isEnrolled = classRepository.isStudentEnrolledInCourse(courseId, user.getId());
                }
                if (!isEnrolled) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Unauthorized: You are not enrolled in this course"));
                }
            }

            // ===========================
            // SUCCESS → RETURN COURSE
            // ===========================
            return ResponseEntity.ok(course);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Failed to fetch course: " + e.getMessage()));
        }
    }

    // ✅ Update course
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> updateCourse(@PathVariable Long id,
                                        @RequestBody Map<String, String> request,
                                        Authentication authentication) {
        try {
            String username = authentication.getName();
            User teacher = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

            Optional<Course> courseOptional = courseService.getCourseById(id);
            if (courseOptional.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Course not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            Course existingCourse = courseOptional.get();
            // Verify course belongs to teacher
            if (!existingCourse.getTeacherId().equals(teacher.getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Unauthorized: Course does not belong to this teacher");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            // Update course fields
            if (request.containsKey("name")) {
                existingCourse.setName(request.get("name"));
            }
            if (request.containsKey("description")) {
                existingCourse.setDescription(request.get("description"));
            }

            Course updatedCourse = courseService.updateCourse(id, existingCourse);
            return ResponseEntity.ok(convertCourseToResponse(updatedCourse));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to update course: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ Delete course
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> deleteCourse(@PathVariable Long id, Authentication authentication) {
        try {
            String username = authentication.getName();
            User teacher = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

            Optional<Course> courseOptional = courseService.getCourseById(id);
            if (courseOptional.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Course not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            Course course = courseOptional.get();
            // Verify course belongs to teacher
            if (!course.getTeacherId().equals(teacher.getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Unauthorized: Course does not belong to this teacher");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            courseService.deleteCourse(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to delete course: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Helper method to convert Course to frontend response format
    private Map<String, Object> convertCourseToResponse(Course course) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", course.getId());
        response.put("name", course.getName());
        response.put("subject", course.getName()); // For frontend compatibility
        response.put("description", course.getDescription() != null ? course.getDescription() : "");
        response.put("teacherId", course.getTeacherId());
        
        // ✅ FIX: Get student count using service method to avoid lazy loading
        try {
            int studentCount = courseService.getStudentCount(course.getId());
            response.put("students", studentCount);
        } catch (Exception e) {
            // Fallback to 0 if there's an error getting student count
            response.put("students", 0);
        }
        
        return response;
    }

    // ✅ NEW: Add student to course
    @PostMapping("/{courseId}/students/{studentId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> addStudentToCourse(@PathVariable Long courseId,
                                              @PathVariable Long studentId,
                                              Authentication authentication) {
        try {
            // Get teacher from authentication
            String username = authentication.getName();
            User teacher = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

            // Verify course exists and belongs to teacher
            Course course = courseService.getCourseById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

            if (!course.getTeacherId().equals(teacher.getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Unauthorized: You don't own this course");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            // Verify student exists
            User student = userService.getUserById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

            // Verify student role
            if (!"STUDENT".equals(student.getRole())) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "User is not a student");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            // Add student to course
            courseService.addStudentToCourse(courseId, studentId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Student added to course successfully");
            response.put("courseId", courseId);
            response.put("studentId", studentId);
            response.put("studentName", student.getName() != null ? student.getName() : student.getUsername());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to add student: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ NEW: Remove student from course
    @DeleteMapping("/{courseId}/students/{studentId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> removeStudentFromCourse(@PathVariable Long courseId,
                                                   @PathVariable Long studentId,
                                                   Authentication authentication) {
        try {
            // Get teacher from authentication
            String username = authentication.getName();
            User teacher = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

            // Verify course exists and belongs to teacher
            Course course = courseService.getCourseById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

            if (!course.getTeacherId().equals(teacher.getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Unauthorized: You don't own this course");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            // Remove student from course
            courseService.removeStudentFromCourse(courseId, studentId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Student removed from course successfully");
            response.put("courseId", courseId);
            response.put("studentId", studentId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to remove student: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ NEW: Get all students in a course
    @GetMapping("/{courseId}/students")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> getCourseStudents(@PathVariable Long courseId,
                                             Authentication authentication) {
        try {
            // Get teacher from authentication
            String username = authentication.getName();
            User teacher = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

            // Verify course exists and belongs to teacher
            Course course = courseService.getCourseById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

            if (!course.getTeacherId().equals(teacher.getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Unauthorized: You don't own this course");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            // Get students enrolled in course
            List<User> students = courseService.getCourseStudents(courseId);

            // Convert to response format
            List<Map<String, Object>> response = students.stream().map(student -> {
                Map<String, Object> studentData = new HashMap<>();
                studentData.put("id", student.getId());
                studentData.put("username", student.getUsername());
                studentData.put("name", student.getName() != null ? student.getName() : student.getUsername());
                studentData.put("email", student.getEmail() != null ? student.getEmail() : "");
                return studentData;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to fetch students: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}