package com.elearnhub.teacher_service.repository;

import com.elearnhub.teacher_service.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByCourseId(Long courseId);
    List<Assignment> findByClassId(Long classId);
    List<Assignment> findByStatus(String status);
    List<Assignment> findByCourseIdAndStatus(Long courseId, String status);
}
