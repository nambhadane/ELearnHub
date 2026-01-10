-- Create user_settings table
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
