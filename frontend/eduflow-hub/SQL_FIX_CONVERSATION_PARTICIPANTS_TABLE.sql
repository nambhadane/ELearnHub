-- ============================================
-- SQL Fix: Ensure conversation_participants table has AUTO_INCREMENT on id
-- ============================================

-- Check current table structure
SHOW CREATE TABLE conversation_participants;

-- If the id column doesn't have AUTO_INCREMENT, run this:
ALTER TABLE conversation_participants 
MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;

-- Verify the change
DESCRIBE conversation_participants;

-- Expected structure:
-- id: BIGINT, NOT NULL, AUTO_INCREMENT, PRIMARY KEY
-- conversation_id: BIGINT, NOT NULL
-- user_id: BIGINT, NOT NULL
-- last_read_at: DATETIME, NULL
-- unread_count: INT, NOT NULL, DEFAULT 0

