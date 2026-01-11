package com.elearnhub.teacher_service.service;

import com.elearnhub.teacher_service.entity.UserSettings;

import java.util.Map;

public interface UserSettingsService {
    UserSettings getUserSettings(Long userId);
    UserSettings updateSettings(Long userId, Map<String, Object> updates);
    UserSettings resetToDefaults(Long userId);
}
