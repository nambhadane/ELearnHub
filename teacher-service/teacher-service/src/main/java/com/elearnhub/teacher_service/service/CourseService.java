package com.elearnhub.teacher_service.service;

import com.elearnhub.teacher_service.entity.Course;
import com.elearnhub.teacher_service.entity.User;
import com.elearnhub.teacher_service.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;
    @Autowired
     private UserService userService;

    public Course createCourse(Course course) {
        // Initialize students list if null to avoid NPE
        if (course.getStudents() == null) {
            course.setStudents(new ArrayList<>());
        }
        return courseRepository.save(course);
    }

    // ✅ Updated: Use repository method that eagerly fetches students
    public List<Course> getCoursesByTeacherId(Long teacherId) {
        // This will eagerly fetch students collection, preventing lazy initialization error
        List<Course> courses = courseRepository.findByTeacherId(teacherId);
        
        // Initialize students if null (defensive programming)
        for (Course course : courses) {
            if (course.getStudents() == null) {
                course.setStudents(new ArrayList<>());
            }
        }
        
        return courses;
    }

    // ✅ Updated: Eagerly fetch students when getting by ID
    public Optional<Course> getCourseById(Long id) {
        return courseRepository.findById(id).map(course -> {
            // Initialize students if null
            if (course.getStudents() == null) {
                course.setStudents(new ArrayList<>());
            }
            // Force initialization of students collection within transaction
            course.getStudents().size(); // This triggers lazy loading within transaction
            return course;
        });
    }

    public Course updateCourse(Long id, Course course) {
        Optional<Course> existingCourse = courseRepository.findById(id);
        if (existingCourse.isPresent()) {
            Course updatedCourse = existingCourse.get();
            updatedCourse.setName(course.getName());
            updatedCourse.setDescription(course.getDescription());
            updatedCourse.setTeacherId(course.getTeacherId());
            // Don't update students list here (that's handled separately)
            return courseRepository.save(updatedCourse);
        }
        throw new RuntimeException("Course not found with id: " + id);
    }

    public void deleteCourse(Long id) {
        courseRepository.deleteById(id);
    }
    
    
    public List<Course> getCoursesByStudentId(Long studentId) {
        // This eagerly fetches students collection using @EntityGraph
        List<Course> courses = courseRepository.findCoursesByStudentId(studentId);
        
        // Initialize students if null (defensive programming)
        if (courses != null) {
            for (Course course : courses) {
                if (course.getStudents() == null) {
                    course.setStudents(new ArrayList<>());
                }
            }
        }
        
        return courses;
    }
    
    // ✅ NEW: Add student to course
    @Transactional
    public void addStudentToCourse(Long courseId, Long studentId) {
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (courseOpt.isEmpty()) {
            throw new RuntimeException("Course not found");
        }

        Course course = courseOpt.get();
        
        // Initialize students list if null
        if (course.getStudents() == null) {
            course.setStudents(new ArrayList<>());
        }

        // Check if student is already enrolled
        boolean alreadyEnrolled = course.getStudents().stream()
                .anyMatch(student -> student.getId().equals(studentId));

        if (alreadyEnrolled) {
            throw new RuntimeException("Student is already enrolled in this course");
        }

        // Get student entity
        User student = userService.getUserById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // Add student to course
        course.getStudents().add(student);
        courseRepository.save(course);

        System.out.println("✅ Student " + student.getUsername() + " added to course " + course.getName());
    }

    // ✅ NEW: Remove student from course
    @Transactional
    public void removeStudentFromCourse(Long courseId, Long studentId) {
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (courseOpt.isEmpty()) {
            throw new RuntimeException("Course not found");
        }

        Course course = courseOpt.get();

        if (course.getStudents() == null || course.getStudents().isEmpty()) {
            throw new RuntimeException("No students enrolled in this course");
        }

        // Remove student from course
        boolean removed = course.getStudents().removeIf(student -> student.getId().equals(studentId));

        if (!removed) {
            throw new RuntimeException("Student is not enrolled in this course");
        }

        courseRepository.save(course);
        System.out.println("✅ Student removed from course " + course.getName());
    }

    // ✅ NEW: Get all students in a course
    public List<User> getCourseStudents(Long courseId) {
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (courseOpt.isEmpty()) {
            throw new RuntimeException("Course not found");
        }

        Course course = courseOpt.get();

        if (course.getStudents() == null) {
            return new ArrayList<>();
        }

        return new ArrayList<>(course.getStudents());
    }
}

