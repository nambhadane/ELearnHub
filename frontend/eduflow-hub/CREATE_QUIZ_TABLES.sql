-- Quiz System Tables

-- Quizzes table
CREATE TABLE IF NOT EXISTS quizzes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    duration INT NOT NULL COMMENT 'Duration in minutes',
    total_marks INT NOT NULL,
    passing_marks INT NOT NULL,
    randomize_questions BOOLEAN NOT NULL DEFAULT FALSE,
    show_results_immediately BOOLEAN NOT NULL DEFAULT TRUE,
    max_attempts INT NOT NULL DEFAULT 1,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT, PUBLISHED, CLOSED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME,
    FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE,
    INDEX idx_class_id (class_id),
    INDEX idx_status (status)
);

-- Questions table
CREATE TABLE IF NOT EXISTS questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    quiz_id BIGINT NOT NULL,
    question_text TEXT NOT NULL,
    question_type VARCHAR(50) NOT NULL COMMENT 'MULTIPLE_CHOICE, TRUE_FALSE, SHORT_ANSWER',
    marks INT NOT NULL,
    order_index INT NOT NULL,
    correct_answer TEXT COMMENT 'For SHORT_ANSWER and TRUE_FALSE',
    explanation TEXT COMMENT 'Optional explanation shown after submission',
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE,
    INDEX idx_quiz_id (quiz_id),
    INDEX idx_order (quiz_id, order_index)
);

-- Question options table (for multiple choice questions)
CREATE TABLE IF NOT EXISTS question_options (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_id BIGINT NOT NULL,
    option_text TEXT NOT NULL,
    is_correct BOOLEAN NOT NULL DEFAULT FALSE,
    order_index INT NOT NULL,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    INDEX idx_question_id (question_id)
);

-- Quiz attempts table
CREATE TABLE IF NOT EXISTS quiz_attempts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    quiz_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    attempt_number INT NOT NULL,
    started_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    submitted_at DATETIME,
    score INT,
    total_marks INT,
    status VARCHAR(50) NOT NULL DEFAULT 'IN_PROGRESS' COMMENT 'IN_PROGRESS, SUBMITTED, AUTO_SUBMITTED',
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_quiz_student (quiz_id, student_id),
    INDEX idx_student_id (student_id),
    INDEX idx_status (status)
);

-- Student answers table
CREATE TABLE IF NOT EXISTS student_answers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    attempt_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    answer_text TEXT COMMENT 'For SHORT_ANSWER',
    selected_option_id BIGINT COMMENT 'For MULTIPLE_CHOICE',
    is_correct BOOLEAN,
    marks_awarded INT,
    FOREIGN KEY (attempt_id) REFERENCES quiz_attempts(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    FOREIGN KEY (selected_option_id) REFERENCES question_options(id) ON DELETE SET NULL,
    INDEX idx_attempt_id (attempt_id),
    INDEX idx_question_id (question_id)
);

-- Sample data for testing
-- INSERT INTO quizzes (class_id, title, description, start_time, end_time, duration, total_marks, passing_marks, status)
-- VALUES (1, 'Java Basics Quiz', 'Test your knowledge of Java fundamentals', 
--         NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 30, 100, 60, 'PUBLISHED');
