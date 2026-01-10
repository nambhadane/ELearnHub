-- Create live_classes table
CREATE TABLE IF NOT EXISTS live_classes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    scheduled_start_time DATETIME NOT NULL,
    scheduled_end_time DATETIME NOT NULL,
    actual_start_time DATETIME,
    actual_end_time DATETIME,
    status VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED',
    meeting_id VARCHAR(255) UNIQUE,
    meeting_password VARCHAR(255),
    host_id BIGINT NOT NULL,
    recording_url VARCHAR(500),
    allow_recording BOOLEAN NOT NULL DEFAULT FALSE,
    allow_chat BOOLEAN NOT NULL DEFAULT TRUE,
    allow_screen_share BOOLEAN NOT NULL DEFAULT TRUE,
    max_participants INT NOT NULL DEFAULT 100,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME,
    FOREIGN KEY (class_id) REFERENCES class_entity(id) ON DELETE CASCADE,
    FOREIGN KEY (host_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_class_id (class_id),
    INDEX idx_status (status),
    INDEX idx_meeting_id (meeting_id),
    INDEX idx_scheduled_start (scheduled_start_time)
);

-- Create live_class_participants table to track who joined
CREATE TABLE IF NOT EXISTS live_class_participants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    live_class_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    left_at DATETIME,
    duration_minutes INT,
    FOREIGN KEY (live_class_id) REFERENCES live_classes(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_live_class (live_class_id),
    INDEX idx_user (user_id)
);
