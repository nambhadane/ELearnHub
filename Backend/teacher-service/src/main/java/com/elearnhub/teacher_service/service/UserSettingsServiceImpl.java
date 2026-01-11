package com.elearnhub.teacher_service.service;

import com.elearnhub.teacher_service.entity.UserSettings;
import com.elearnhub.teacher_service.repository.UserSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional
public class UserSettingsServiceImpl implements UserSettingsService {
    
    @Autowired
    private UserSettingsRepository settingsRepository;
    
    @Override
    @Transactional(readOnly = true)
    public UserSettings getUserSettings(Long userId) {
        return settingsRepository.findByUserId(userId)
                .orElseGet(() -> {
                    // Create default settings if none exist
                    UserSettings settings = new UserSettings(userId);
                    return settingsRepository.save(settings);
                });
    }
    
    @Override
    public UserSettings updateSettings(Long userId, Map<String, Object> updates) {
        UserSettings settings = getUserSettings(userId);
        
        // Update appearance settings
        if (updates.containsKey("theme")) {
            settings.setTheme((String) updates.get("theme"));
        }
        if (updates.containsKey("language")) {
            settings.setLanguage((String) updates.get("language"));
        }
        
        // Update notification settings
        if (updates.containsKey("emailNotifications")) {
            settings.setEmailNotifications((Boolean) updates.get("emailNotifications"));
        }
        if (updates.containsKey("pushNotifications")) {
            settings.setPushNotifications((Boolean) updates.get("pushNotifications"));
        }
        if (updates.containsKey("assignmentReminders")) {
            settings.setAssignmentReminders((Boolean) updates.get("assignmentReminders"));
        }
        if (updates.containsKey("gradeNotifications")) {
            settings.setGradeNotifications((Boolean) updates.get("gradeNotifications"));
        }
        if (updates.containsKey("messageNotifications")) {
            settings.setMessageNotifications((Boolean) updates.get("messageNotifications"));
        }
        
        // Update privacy settings
        if (updates.containsKey("profileVisible")) {
            settings.setProfileVisible((Boolean) updates.get("profileVisible"));
        }
        if (updates.containsKey("showEmail")) {
            settings.setShowEmail((Boolean) updates.get("showEmail"));
        }
        if (updates.containsKey("showPhone")) {
            settings.setShowPhone((Boolean) updates.get("showPhone"));
        }
        
        // Update display preferences
        if (updates.containsKey("itemsPerPage")) {
            settings.setItemsPerPage((Integer) updates.get("itemsPerPage"));
        }
        if (updates.containsKey("dateFormat")) {
            settings.setDateFormat((String) updates.get("dateFormat"));
        }
        if (updates.containsKey("timeFormat")) {
            settings.setTimeFormat((String) updates.get("timeFormat"));
        }
        
        UserSettings saved = settingsRepository.save(settings);
        System.out.println("✅ Settings updated for user: " + userId);
        return saved;
    }
    
    @Override
    public UserSettings resetToDefaults(Long userId) {
        UserSettings settings = getUserSettings(userId);
        
        // Reset to defaults
        settings.setTheme("light");
        settings.setLanguage("en");
        settings.setEmailNotifications(true);
        settings.setPushNotifications(true);
        settings.setAssignmentReminders(true);
        settings.setGradeNotifications(true);
        settings.setMessageNotifications(true);
        settings.setProfileVisible(true);
        settings.setShowEmail(false);
        settings.setShowPhone(false);
        settings.setItemsPerPage(10);
        settings.setDateFormat("MM/DD/YYYY");
        settings.setTimeFormat("12h");
        
        UserSettings saved = settingsRepository.save(settings);
        System.out.println("✅ Settings reset to defaults for user: " + userId);
        return saved;
    }
}
