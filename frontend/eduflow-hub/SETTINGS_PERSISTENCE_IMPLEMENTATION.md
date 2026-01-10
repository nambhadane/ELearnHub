# Settings Persistence Feature - Implementation Guide

## ✅ What's Been Created

### Backend Files (Copy to your backend project)

1. **UserSettings.java** → `teacher-service/src/main/java/com/elearnhub/teacher_service/entity/UserSettings.java`
   - Entity for storing user preferences

2. **UserSettingsRepository.java** → `teacher-service/src/main/java/com/elearnhub/teacher_service/repository/UserSettingsRepository.java`
   - Database repository

3. **UserSettingsService.java** → `teacher-service/src/main/java/com/elearnhub/teacher_service/service/UserSettingsService.java`
   - Service interface

4. **UserSettingsServiceImpl.java** → `teacher-service/src/main/java/com/elearnhub/teacher_service/service/UserSettingsServiceImpl.java`
   - Service implementation with auto-creation of defaults

5. **UserSettingsController.java** → `teacher-service/src/main/java/com/elearnhub/teacher_service/Controller/UserSettingsController.java`
   - REST API endpoints

### Database

6. **CREATE_USER_SETTINGS_TABLE.sql**
   - Run this SQL to create the user_settings table

### Frontend

7. **src/services/api.ts** - Settings API functions added

## 📋 Setup Steps

### 1. Database Setup
```sql
CREATE TABLE IF NOT EXISTS user_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    
    -- Appearance settings
    theme VARCHAR(20) NOT NULL DEFAULT 'light',
    language VARCHAR(10) NOT NULL DEFAULT 'en',
    
    -- Notification settings
    email_notifications BOOLEAN NOT NULL DEFAULT TRUE,
    push_notifications BOOLEAN NOT NULL DEFAULT TRUE,
    assignment_reminders BOOLEAN NOT NULL DEFAULT TRUE,
    grade_notifications BOOLEAN NOT NULL DEFAULT TRUE,
    message_notifications BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- Privacy settings
    profile_visible BOOLEAN NOT NULL DEFAULT TRUE,
    show_email BOOLEAN NOT NULL DEFAULT FALSE,
    show_phone BOOLEAN NOT NULL DEFAULT FALSE,
    
    -- Display preferences
    items_per_page INT NOT NULL DEFAULT 10,
    date_format VARCHAR(20) NOT NULL DEFAULT 'MM/DD/YYYY',
    time_format VARCHAR(10) NOT NULL DEFAULT '12h',
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_user_id (user_id)
);
```

### 2. Copy Backend Files

Copy all UserSettings*.java files to your backend project.

### 3. Restart Backend

Restart your Spring Boot application.

## 🎯 Features

### Settings Categories:

**1. Appearance Settings:**
- ✅ Theme (light, dark, system)
- ✅ Language (en, es, fr, etc.)

**2. Notification Settings:**
- ✅ Email notifications
- ✅ Push notifications
- ✅ Assignment reminders
- ✅ Grade notifications
- ✅ Message notifications

**3. Privacy Settings:**
- ✅ Profile visibility
- ✅ Show email publicly
- ✅ Show phone publicly

**4. Display Preferences:**
- ✅ Items per page
- ✅ Date format (MM/DD/YYYY, DD/MM/YYYY, YYYY-MM-DD)
- ✅ Time format (12h, 24h)

### Key Features:
- ✅ Auto-creates default settings on first access
- ✅ Persists across sessions and devices
- ✅ Reset to defaults option
- ✅ Partial updates (only send changed fields)
- ✅ Works for all user roles (Teacher, Student, Admin)

## 🔌 API Endpoints

### Get User Settings
```
GET /settings
Authorization: Bearer {token}

Response:
{
  "id": 1,
  "userId": 14,
  "theme": "light",
  "language": "en",
  "emailNotifications": true,
  "pushNotifications": true,
  "assignmentReminders": true,
  "gradeNotifications": true,
  "messageNotifications": true,
  "profileVisible": true,
  "showEmail": false,
  "showPhone": false,
  "itemsPerPage": 10,
  "dateFormat": "MM/DD/YYYY",
  "timeFormat": "12h"
}
```

### Update Settings
```
PUT /settings
Authorization: Bearer {token}
Content-Type: application/json

Body (partial update):
{
  "theme": "dark",
  "emailNotifications": false,
  "itemsPerPage": 20
}

Response:
{
  "message": "Settings updated successfully",
  "settings": { ... }
}
```

### Reset to Defaults
```
POST /settings/reset
Authorization: Bearer {token}

Response:
{
  "message": "Settings reset to defaults",
  "settings": { ... }
}
```

## 💻 Frontend Integration

### Example Usage:

```typescript
import { getUserSettings, updateUserSettings, resetUserSettings } from "@/services/api";

// Load settings on app start
const settings = await getUserSettings();

// Update theme
await updateUserSettings({ theme: "dark" });

// Update multiple settings
await updateUserSettings({
  theme: "dark",
  emailNotifications: false,
  itemsPerPage: 20
});

// Reset to defaults
await resetUserSettings();
```

### Integration with Settings Page:

Update your Settings page (student/teacher) to:
1. Load settings on mount
2. Update settings when user changes them
3. Show loading/success states
4. Add "Reset to Defaults" button

**Example:**
```typescript
const [settings, setSettings] = useState<UserSettings | null>(null);

useEffect(() => {
  const loadSettings = async () => {
    const data = await getUserSettings();
    setSettings(data);
  };
  loadSettings();
}, []);

const handleThemeChange = async (theme: string) => {
  const updated = await updateUserSettings({ theme });
  setSettings(updated);
};
```

## 🎨 Default Values

When a user first accesses settings, these defaults are created:

- **Theme:** light
- **Language:** en
- **Email Notifications:** true
- **Push Notifications:** true
- **Assignment Reminders:** true
- **Grade Notifications:** true
- **Message Notifications:** true
- **Profile Visible:** true
- **Show Email:** false
- **Show Phone:** false
- **Items Per Page:** 10
- **Date Format:** MM/DD/YYYY
- **Time Format:** 12h

## 🔒 Security

- ✅ Users can only access/update their own settings
- ✅ JWT authentication required
- ✅ Settings tied to user ID
- ✅ Cascade delete when user is deleted

## 🚀 Next Steps

### Update Settings Pages:

**Student Settings (`src/pages/student/Settings.tsx`):**
- Load settings from backend
- Save changes to backend
- Add reset button

**Teacher Settings (`src/pages/teacher/Settings.tsx`):**
- Load settings from backend
- Save changes to backend
- Add reset button

### Example Implementation:

```typescript
// In Settings.tsx
const [settings, setSettings] = useState<UserSettings | null>(null);
const [loading, setLoading] = useState(true);

useEffect(() => {
  const loadSettings = async () => {
    try {
      const data = await getUserSettings();
      setSettings(data);
    } catch (error) {
      toast({ title: "Error", description: "Failed to load settings" });
    } finally {
      setLoading(false);
    }
  };
  loadSettings();
}, []);

const handleSave = async (updates: Partial<UserSettings>) => {
  try {
    const updated = await updateUserSettings(updates);
    setSettings(updated);
    toast({ title: "Success", description: "Settings saved" });
  } catch (error) {
    toast({ title: "Error", description: "Failed to save settings" });
  }
};
```

## ✨ Optional Enhancements

- [ ] Settings sync indicator
- [ ] Last updated timestamp
- [ ] Settings export/import
- [ ] Settings history/audit log
- [ ] Per-device settings
- [ ] Settings presets (themes)
- [ ] Advanced notification preferences
- [ ] Accessibility settings

## 🎉 You're Done!

The Settings Persistence backend is complete! Users' preferences will now be saved to the database and persist across sessions.

**To complete the feature:**
1. Run the SQL to create the table
2. Copy backend files
3. Restart backend
4. Update Settings pages to use the API
5. Test by changing settings and logging out/in!
