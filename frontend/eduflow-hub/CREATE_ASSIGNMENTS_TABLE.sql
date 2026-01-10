-- Create assignments table
CREATE TABLE IF NOT EXISTS assignment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    due_date DATETIME NOT NULL,
    max_grade DOUBLE NOT NULL,
    course_id BIGINT NOT NULL,
    weight DOUBLE,
    allow_late_submission BOOLEAN NOT NULL DEFAULT FALSE,
    late_penalty DOUBLE,
    additional_instructions VARCHAR(1000),
    status VARCHAR(20) NOT NULL DEFAULT 'published',
    created_at DATETIME,
    updated_at DATETIME,
    CONSTRAINT fk_assignment_course FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE
);

-- Create index for faster queries
CREATE INDEX idx_assignment_course_id ON assignment(course_id);
CREATE INDEX idx_assignment_status ON assignment(status);
CREATE INDEX idx_assignment_due_date ON assignment(due_date);

-- If the table already exists but is missing columns, use these ALTER statements:
-- ALTER TABLE assignment ADD COLUMN weight DOUBLE;
-- ALTER TABLE assignment ADD COLUMN allow_late_submission BOOLEAN NOT NULL DEFAULT FALSE;
-- ALTER TABLE assignment ADD COLUMN late_penalty DOUBLE;
-- ALTER TABLE assignment ADD COLUMN additional_instructions VARCHAR(1000);
-- ALTER TABLE assignment ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'published';
-- ALTER TABLE assignment ADD COLUMN created_at DATETIME;
-- ALTER TABLE assignment ADD COLUMN updated_at DATETIME;
