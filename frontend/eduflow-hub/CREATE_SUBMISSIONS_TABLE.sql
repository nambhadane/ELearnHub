-- Create submissions table for assignment submissions
CREATE TABLE IF NOT EXISTS submission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    assignment_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    content TEXT,
    file_path VARCHAR(500),
    submitted_at DATETIME NOT NULL,
    grade DOUBLE,
    feedback TEXT,
    graded_at DATETIME,
    
    -- Foreign key constraints
    CONSTRAINT fk_submission_assignment 
        FOREIGN KEY (assignment_id) REFERENCES assignment(id) ON DELETE CASCADE,
    CONSTRAINT fk_submission_student 
        FOREIGN KEY (student_id) REFERENCES user(id) ON DELETE CASCADE,
    
    -- Indexes for better performance
    INDEX idx_submission_assignment (assignment_id),
    INDEX idx_submission_student (student_id),
    INDEX idx_submission_assignment_student (assignment_id, student_id)
);

COMMIT;