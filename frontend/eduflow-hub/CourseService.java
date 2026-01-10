package com.elearnhub.teacher_service.service;

import com.elearnhub.teacher_service.entity.Course;
import com.elearnhub.teacher_service.entity.User;
import java.util.List;
import java.util.Optional;

public interface CourseService {
    
    /**
     * Create a new course
     */
    Course createCourse(Course course);
    
    /**
     * Get all courses for a specific teacher
     */
    List<Course> getCoursesByTeacherId(Long teacherId);
    
    /**
     * Get course by ID
     */
    Optional<Course> getCourseById(Long id);
    
    /**
     * Update an existing course
     */
    Course updateCourse(Long id, Course course);
    
    /**
     * Delete a course
     */
    void deleteCourse(Long id);
    
    /**
     * Get courses by student ID
     */
    List<Course> getCoursesByStudentId(Long studentId);
    
    /**
     * Add student to course
     */
    void addStudentToCourse(Long courseId, Long studentId);
    
    /**
     * Remove student from course
     */
    void removeStudentFromCourse(Long courseId, Long studentId);
    
    /**
     * Get all students in a course
     */
    List<User> getCourseStudents(Long courseId);
    
    /**
     * Get student count for a course
     */
    int getStudentCount(Long courseId);
}