package com.elearnhub.teacher_service.repository;

import com.elearnhub.teacher_service.entity.ClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassEntityRepository extends JpaRepository<ClassEntity, Long> {
	// Check if student is enrolled in a course
    @Query(value = "SELECT COUNT(*) > 0 FROM class_entity c " +
           "JOIN class_student cs ON cs.class_id = c.id " +
           "WHERE c.course_id = :courseId AND cs.student_id = :studentId", nativeQuery = true)
    boolean isStudentEnrolledInCourse(@Param("courseId") Long courseId, @Param("studentId") Long studentId);
    
    // Find classes by teacher ID
    List<ClassEntity> findByTeacherId(Long teacherId);
    
    // Find classes by teacher ID with students eagerly loaded
    @Query("SELECT DISTINCT c FROM ClassEntity c " +
           "LEFT JOIN FETCH c.students " +
           "WHERE c.teacher.id = :teacherId")
    List<ClassEntity> findByTeacherIdWithStudents(@Param("teacherId") Long teacherId);
    
    // Find classes by student ID (using @ManyToMany relationship)
    @Query("SELECT DISTINCT c FROM ClassEntity c " +
           "JOIN c.students s " +
           "WHERE s.id = :studentId")
    List<ClassEntity> findClassesByStudentId(@Param("studentId") Long studentId);
    
    // Find class by ID with students eagerly loaded (teacher and course loaded separately)
    @Query("SELECT DISTINCT c FROM ClassEntity c " +
           "LEFT JOIN FETCH c.students " +
           "WHERE c.id = :classId")
    Optional<ClassEntity> findByIdWithStudents(@Param("classId") Long classId);
    
    // Find class by ID with all relationships eagerly loaded
    @Query("SELECT DISTINCT c FROM ClassEntity c " +
           "LEFT JOIN FETCH c.teacher " +
           "LEFT JOIN FETCH c.course " +
           "LEFT JOIN FETCH c.students " +
           "WHERE c.id = :classId")
    Optional<ClassEntity> findByIdWithAllRelationships(@Param("classId") Long classId);
}
