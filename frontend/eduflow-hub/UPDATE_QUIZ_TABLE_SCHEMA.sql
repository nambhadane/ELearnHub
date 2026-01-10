-- Update Quiz table schema to match the new entity structure
-- Run this SQL script to update your database

-- Add new columns if they don't exist
ALTER TABLE quizzes 
ADD COLUMN IF NOT EXISTS course_id BIGINT,
ADD COLUMN IF NOT EXISTS due_date TIMESTAMP,
ADD COLUMN IF NOT EXISTS time_limit INTEGER,
ADD COLUMN IF NOT EXISTS max_grade INTEGER,
ADD COLUMN IF NOT EXISTS shuffle_questions BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS show_results BOOLEAN DEFAULT TRUE,
ADD COLUMN IF NOT EXISTS allow_retakes BOOLEAN DEFAULT FALSE;

-- Copy data from old columns to new columns (if migrating existing data)
UPDATE quizzes SET 
    course_id = class_id,
    due_date = end_time,
    time_limit = duration,
    max_grade = total_marks,
    shuffle_questions = COALESCE(randomize_questions, FALSE),
    show_results = COALESCE(show_results_immediately, TRUE),
    allow_retakes = FALSE
WHERE course_id IS NULL;

-- Optional: Drop old columns after data migration (uncomment if needed)
-- ALTER TABLE quizzes 
-- DROP COLUMN IF EXISTS class_id,
-- DROP COLUMN IF EXISTS end_time,
-- DROP COLUMN IF EXISTS duration,
-- DROP COLUMN IF EXISTS total_marks,
-- DROP COLUMN IF EXISTS randomize_questions,
-- DROP COLUMN IF EXISTS show_results_immediately;

-- Add foreign key constraint for course_id if courses table exists
-- ALTER TABLE quizzes 
-- ADD CONSTRAINT fk_quiz_course 
-- FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE;

-- Update any existing quiz records to have proper status
UPDATE quizzes SET status = 'DRAFT' WHERE status IS NULL OR status = '';
UPDATE quizzes SET status = 'PUBLISHED' WHERE status = 'ACTIVE';

-- Ensure all boolean fields have proper default values
UPDATE quizzes SET 
    shuffle_questions = FALSE WHERE shuffle_questions IS NULL,
    show_results = TRUE WHERE show_results IS NULL,
    allow_retakes = FALSE WHERE allow_retakes IS NULL;