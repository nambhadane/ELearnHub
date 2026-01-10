-- Change Bhagyashri's role from STUDENT to TEACHER
UPDATE users SET role = 'TEACHER' WHERE username = 'Bhagyashri';

-- Verify the change
SELECT id, username, name, email, role FROM users WHERE username = 'Bhagyashri';
