-- Add class_id column to assignment table
-- This allows assignments to be associated with specific classes

ALTER TABLE assignment 
ADD COLUMN class_id BIGINT;

-- Add foreign key constraint (optional, but recommended)
-- ALTER TABLE assignment 
-- ADD CONSTRAINT fk_assignment_class 
-- FOREIGN KEY (class_id) REFERENCES class_entity(id);

-- Add index for better query performance
CREATE INDEX idx_assignment_class_id ON assignment(class_id);

-- Update existing assignments to have class_id based on course_id
-- This is a temporary solution - in production, you'd need proper data migration
-- UPDATE assignment a 
-- SET class_id = (
--     SELECT c.id 
--     FROM class_entity c 
--     WHERE c.course_id = a.course_id 
--     LIMIT 1
-- )
-- WHERE a.class_id IS NULL;

COMMIT;