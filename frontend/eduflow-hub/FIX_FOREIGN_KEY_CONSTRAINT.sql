-- ============================================
-- FIX FOREIGN KEY CONSTRAINT FOR class_student
-- ============================================
-- Problem: Foreign key references 'user' but table is 'users'
-- Solution: Drop old constraint and create new one with correct table name
-- ============================================

-- Step 1: Drop the old foreign key constraint
ALTER TABLE class_student 
DROP FOREIGN KEY FK7sbm5g487ge9v25pyh8tvs8lg;

-- Step 2: Create new foreign key constraint referencing 'users' table
ALTER TABLE class_student 
ADD CONSTRAINT FK_class_student_student_id 
FOREIGN KEY (student_id) 
REFERENCES users(id) 
ON DELETE CASCADE 
ON UPDATE CASCADE;

-- Step 3: Verify the constraint was created correctly
SELECT 
    CONSTRAINT_NAME,
    TABLE_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_NAME = 'class_student' 
  AND CONSTRAINT_NAME = 'FK_class_student_student_id';

-- Expected result: REFERENCED_TABLE_NAME should be 'users' (not 'user')






