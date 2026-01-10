-- Create schedules table
CREATE TABLE IF NOT EXISTS schedules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_id BIGINT NOT NULL,
    day_of_week VARCHAR(20) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    room VARCHAR(100),
    location VARCHAR(255),
    notes VARCHAR(500),
    
    FOREIGN KEY (class_id) REFERENCES class_entity(id) ON DELETE CASCADE,
    
    INDEX idx_class_id (class_id),
    INDEX idx_day_of_week (day_of_week)
);
