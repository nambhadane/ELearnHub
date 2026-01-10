-- Create notifications table
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT,
    type VARCHAR(50) NOT NULL,
    reference_id BIGINT,
    reference_type VARCHAR(50),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP NULL,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read),
    INDEX idx_created_at (created_at),
    INDEX idx_user_unread (user_id, is_read)
);

-- Sample notifications for testing
-- INSERT INTO notifications (user_id, title, message, type, reference_id, reference_type, is_read) 
-- VALUES 
-- (1, 'New Message', 'You have a new message from John Doe', 'MESSAGE', 1, 'MESSAGE', FALSE),
-- (1, 'Assignment Graded', 'Your assignment "Java Basics" has been graded', 'GRADE', 5, 'ASSIGNMENT', FALSE);
