package com.elearnhub.teacher_service.repository;

import com.elearnhub.teacher_service.entity.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserSettingsRepository extends JpaRepository<UserSettings, Long> {
    Optional<UserSettings> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}
