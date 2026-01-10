-- Fix Course-Class Relationship for Quiz Creation
-- Run this script to ensure courses exist and classes are properly linked

-- First, let's see what we have
SELECT 'Current Courses:' as info;
SELECT id, name, teacher_id FROM courses;

SELECT 'Current Classes:' as info;
SELECT id, name, teacher_id, course_id FROM class_entity;

-- Insert default courses if they don't exist
INSERT IGNORE INTO courses (id, name, description, teacher_id) VALUES
(1, 'Mathematics 101', 'Basic Mathematics Course', NULL),
(2, 'Physics 101', 'Introduction to Physics', NULL),
(3, 'Computer Science 101', 'Programming Fundamentals', NULL),
(4, 'English Literature', 'English Literature Course', NULL),
(5, 'History 101', 'World History Course', NULL);

-- Update classes that don't have a course_id to link them to course 1
UPDATE class_entity 
SET course_id = 1 
WHERE course_id IS NULL OR course_id NOT IN (SELECT id FROM courses);

-- Verify the fix
SELECT 'After Fix - Classes with Course Info:' as info;
SELECT 
    ce.id as class_id,
    ce.name as class_name,
    ce.course_id,
    c.name as course_name
FROM class_entity ce
LEFT JOIN courses c ON ce.course_id = c.id;

-- Check if there are any classes without valid course references
SELECT 'Classes without valid courses:' as info;
SELECT ce.id, ce.name, ce.course_id
FROM class_entity ce
LEFT JOIN courses c ON ce.course_id = c.id
WHERE c.id IS NULL;

COMMIT;