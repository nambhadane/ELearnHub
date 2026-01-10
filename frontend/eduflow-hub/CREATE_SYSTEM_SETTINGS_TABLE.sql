-- Create system_settings table for admin configuration
CREATE TABLE IF NOT EXISTS system_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- General Settings
    platform_name VARCHAR(255) NOT NULL DEFAULT 'EduFlow Hub',
    platform_description TEXT,
    support_email VARCHAR(255) NOT NULL DEFAULT 'support@eduflow.com',
    max_file_upload_size INT NOT NULL DEFAULT 50,
    session_timeout INT NOT NULL DEFAULT 30,
    
    -- User Management
    allow_self_registration BOOLEAN NOT NULL DEFAULT TRUE,
    require_email_verification BOOLEAN NOT NULL DEFAULT FALSE,
    default_user_role VARCHAR(50) NOT NULL DEFAULT 'STUDENT',
    password_min_length INT NOT NULL DEFAULT 8,
    password_require_special_chars BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- Notifications
    email_notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    push_notifications_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    notification_retention_days INT NOT NULL DEFAULT 30,
    
    -- Security
    enable_two_factor_auth BOOLEAN NOT NULL DEFAULT FALSE,
    max_login_attempts INT NOT NULL DEFAULT 5,
    lockout_duration_minutes INT NOT NULL DEFAULT 15,
    
    -- Academic Settings
    default_grading_scale VARCHAR(50) NOT NULL DEFAULT 'PERCENTAGE',
    allow_late_submissions BOOLEAN NOT NULL DEFAULT TRUE,
    default_late_penalty INT NOT NULL DEFAULT 10,
    academic_year_start DATE NOT NULL DEFAULT '2024-09-01',
    academic_year_end DATE NOT NULL DEFAULT '2025-06-30',
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP
);

-- Insert default settings if table is empty
INSERT INTO system_settings (
    platform_name,
    platform_description,
    support_email,
    max_file_upload_size,
    session_timeout,
    allow_self_registration,
    require_email_verification,
    default_user_role,
    password_min_length,
    password_require_special_chars,
    email_notifications_enabled,
    push_notifications_enabled,
    notification_retention_days,
    enable_two_factor_auth,
    max_login_attempts,
    lockout_duration_minutes,
    default_grading_scale,
    allow_late_submissions,
    default_late_penalty,
    academic_year_start,
    academic_year_end
) 
SELECT 
    'EduFlow Hub',
    'Comprehensive Learning Management System',
    'support@eduflow.com',
    50,
    30,
    TRUE,
    FALSE,
    'STUDENT',
    8,
    TRUE,
    TRUE,
    FALSE,
    30,
    FALSE,
    5,
    15,
    'PERCENTAGE',
    TRUE,
    10,
    '2024-09-01',
    '2025-06-30'
WHERE NOT EXISTS (SELECT 1 FROM system_settings);