-- Update Quiz Database Schema to Match Entity Mappings
-- Run this script in your MySQL database to fix the quiz system

-- First, check if the tables exist and drop them if they do (to ensure clean schema)
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS student_answers;
DROP TABLE IF EXISTS quiz_attempts;
DROP TABLE IF EXISTS question_options;
DROP TABLE IF EXISTS questions;
DROP TABLE IF EXISTS quizzes;

SET FOREIGN_KEY_CHECKS = 1;

-- Create the quizzes table with correct column names
CREATE TABLE quizzes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    due_date TIMESTAMP NOT NULL,
    start_time TIMESTAMP NOT NULL,
    time_limit INTEGER NOT NULL DEFAULT 60,
    max_grade INTEGER NOT NULL DEFAULT 100,
    passing_marks INTEGER NOT NULL DEFAULT 50,
    shuffle_questions BOOLEAN DEFAULT FALSE,
    show_results BOOLEAN DEFAULT TRUE,
    allow_retakes BOOLEAN DEFAULT FALSE,
    max_attempts INTEGER DEFAULT 1,
    status VARCHAR(20) DEFAULT 'DRAFT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
);

-- Create the questions table with correct column names
CREATE TABLE questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    quiz_id BIGINT NOT NULL,
    question_text TEXT NOT NULL,
    question_type VARCHAR(20) DEFAULT 'MULTIPLE_CHOICE',
    points INTEGER DEFAULT 1,
    order_index INTEGER DEFAULT 0,
    correct_answer TEXT,
    explanation TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE
);

-- Create the question_options table
CREATE TABLE question_options (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_id BIGINT NOT NULL,
    option_text TEXT NOT NULL,
    is_correct BOOLEAN DEFAULT FALSE,
    order_index INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
);

-- Create the quiz_attempts table with correct column names
CREATE TABLE quiz_attempts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    quiz_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    attempt_number INTEGER DEFAULT 1,
    score INTEGER DEFAULT 0,
    max_score INTEGER DEFAULT 0,
    percentage DECIMAL(5,2) DEFAULT 0.00,
    status VARCHAR(20) DEFAULT 'IN_PROGRESS',
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    submitted_at TIMESTAMP NULL,
    time_taken INTEGER DEFAULT 0,
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create the student_answers table with correct column names
CREATE TABLE student_answers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    attempt_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    selected_option_id BIGINT,
    answer_text TEXT,
    is_correct BOOLEAN DEFAULT FALSE,
    points_earned INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (attempt_id) REFERENCES quiz_attempts(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    FOREIGN KEY (selected_option_id) REFERENCES question_options(id) ON DELETE SET NULL
);

-- Create indexes for better performance
CREATE INDEX idx_quizzes_course ON quizzes(course_id);
CREATE INDEX idx_quizzes_status ON quizzes(status);
CREATE INDEX idx_questions_quiz ON questions(quiz_id);
CREATE INDEX idx_question_options_question ON question_options(question_id);
CREATE INDEX idx_quiz_attempts_quiz ON quiz_attempts(quiz_id);
CREATE INDEX idx_quiz_attempts_student ON quiz_attempts(student_id);
CREATE INDEX idx_quiz_attempts_quiz_student ON quiz_attempts(quiz_id, student_id);
CREATE INDEX idx_student_answers_attempt ON student_answers(attempt_id);
CREATE INDEX idx_student_answers_question ON student_answers(question_id);

-- Insert a test quiz for debugging (optional)
INSERT INTO quizzes (course_id, title, description, due_date, start_time, time_limit, max_grade, passing_marks, status) 
VALUES (1, 'Test Quiz', 'A test quiz for debugging', 
        DATE_ADD(NOW(), INTERVAL 7 DAY), 
        NOW(), 
        30, 100, 50, 'PUBLISHED');

-- Verify the table structures
SELECT 'Quizzes table structure:' as info;
DESCRIBE quizzes;

SELECT 'Questions table structure:' as info;
DESCRIBE questions;

SELECT 'Quiz attempts table structure:' as info;
DESCRIBE quiz_attempts;

SELECT 'Student answers table structure:' as info;
DESCRIBE student_answers;

COMMIT;