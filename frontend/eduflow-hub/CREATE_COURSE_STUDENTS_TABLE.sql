-- Create course_students table for many-to-many relationship
-- This table links courses with enrolled students

CREATE TABLE IF NOT EXISTS course_students (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_course_student (course_id, student_id)
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_course_students_course ON course_students(course_id);
CREATE INDEX IF NOT EXISTS idx_course_students_student ON course_students(student_id);

COMMIT;