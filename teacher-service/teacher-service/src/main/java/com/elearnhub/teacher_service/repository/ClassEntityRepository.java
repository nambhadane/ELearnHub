package com.elearnhub.teacher_service.repository;

import com.elearnhub.teacher_service.entity.ClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassEntityRepository extends JpaRepository<ClassEntity, Long> {
    List<ClassEntity> findByTeacherId(Long teacherId);
}