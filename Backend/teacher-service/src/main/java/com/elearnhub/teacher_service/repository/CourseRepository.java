//
//package com.elearnhub.teacher_service.repository;
//
//import com.elearnhub.teacher_service.entity.Course;
//import com.elearnhub.teacher_service.entity.User;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//
//@Repository
//public interface CourseRepository extends JpaRepository<Course, Long> {
//    
//    // Find courses by teacher
//    List<Course> findByTeacher(User teacher);
//    
//    // Find courses by teacher ID
//    @Query("SELECT c FROM Course c WHERE c.teacher.id = :teacherId")
//    List<Course> findByTeacherId(@Param("teacherId") Long teacherId);
//    
//    // Find courses by name containing (case insensitive)
//    @Query("SELECT c FROM Course c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
//    List<Course> findByNameContainingIgnoreCase(@Param("name") String name);
//    
//    // Find courses by description containing (case insensitive)
//    @Query("SELECT c FROM Course c WHERE LOWER(c.description) LIKE LOWER(CONCAT('%', :description, '%'))")
//    List<Course> findByDescriptionContainingIgnoreCase(@Param("description") String description);
//    
//    // Check if course name exists
//    boolean existsByName(String name);
//    
//    // Get courses with teacher information
//    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.teacher")
//    List<Course> findAllWithTeacher();
//    
//    @Query("SELECT DISTINCT c FROM Course c JOIN ClassEntity cl ON cl.course.id = c.id JOIN cl.students s WHERE s.id = :studentId")
//    List<Course> findCoursesByStudentId(@Param("studentId") Long studentId);
//
//}


package com.elearnhub.teacher_service.repository;

import com.elearnhub.teacher_service.entity.Course;
import com.elearnhub.teacher_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    
    // Find courses by teacher ID
    @Query(value = "SELECT * FROM courses WHERE teacher_id = :teacherId", nativeQuery = true)
    List<Course> findByTeacherId(@Param("teacherId") Long teacherId);
    
    // Find courses by name containing (case insensitive)
    @Query("SELECT c FROM Course c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Course> findByNameContainingIgnoreCase(@Param("name") String name);
    
    // Find courses by description containing (case insensitive)
    @Query("SELECT c FROM Course c WHERE LOWER(c.description) LIKE LOWER(CONCAT('%', :description, '%'))")
    List<Course> findByDescriptionContainingIgnoreCase(@Param("description") String description);
    
    // Check if course name exists
    boolean existsByName(String name);
    
    // Get courses with teacher information
    @Query(value = "SELECT c.*, u.id as teacher_id, u.name as teacher_name, u.email as teacher_email " +
           "FROM courses c LEFT JOIN users u ON c.teacher_id = u.id", nativeQuery = true)
    List<Course> findAllWithTeacher();
    
    // Find courses by student ID (through class enrollment)
    @Query(value = "SELECT DISTINCT c.* FROM courses c " +
           "JOIN class_entity cl ON cl.course_id = c.id " +
           "JOIN class_student cs ON cs.class_id = cl.id " +
           "WHERE cs.student_id = :studentId", nativeQuery = true)
    List<Course> findCoursesByStudentId(@Param("studentId") Long studentId);
}