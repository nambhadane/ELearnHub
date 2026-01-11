package com.elearnhub.teacher_service.repository;

import com.elearnhub.teacher_service.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {
    List<Material> findByClassIdOrderByUploadedAtDesc(Long classId);
    List<Material> findByUploadedByOrderByUploadedAtDesc(Long uploadedBy);
}
