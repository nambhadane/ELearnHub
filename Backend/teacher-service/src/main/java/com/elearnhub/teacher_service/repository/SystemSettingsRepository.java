package com.elearnhub.teacher_service.repository;

import com.elearnhub.teacher_service.entity.SystemSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemSettingsRepository extends JpaRepository<SystemSettings, Long> {
    
    // Get the current system settings (there should only be one record)
    @Query("SELECT s FROM SystemSettings s ORDER BY s.id DESC")
    Optional<SystemSettings> findCurrentSettings();
    
    // Check if any settings exist
    @Query("SELECT COUNT(s) > 0 FROM SystemSettings s")
    boolean existsAny();
}