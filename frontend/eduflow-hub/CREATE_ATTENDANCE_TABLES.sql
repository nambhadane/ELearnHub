-- Create attendance_session table
CREATE TABLE IF NOT EXISTS attendance_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_id BIGINT NOT NULL,
    session_date DATE NOT NULL,
    session_time TIME,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    created_by BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME,
    FOREIGN KEY (class_id) REFERENCES class_entity(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_class_date (class_id, session_date),
    INDEX idx_created_by (created_by)
);

-- Create attendance_record table
CREATE TABLE IF NOT EXISTS attendance_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ABSENT',
    marked_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    marked_by BIGINT NOT NULL,
    notes TEXT,
    FOREIGN KEY (session_id) REFERENCES attendance_session(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (marked_by) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_session_student (session_id, student_id),
    INDEX idx_student (student_id),
    INDEX idx_status (status),
    INDEX idx_marked_by (marked_by)
);

-- Add some sample data (optional)
-- INSERT INTO attendance_session (class_id, session_date, session_time, title, description, created_by)
-- VALUES (1, CURDATE(), CURTIME(), 'Today\'s Class', 'Regular class session', 1);
