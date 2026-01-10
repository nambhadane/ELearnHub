# Quiz 400 Error - Complete Fix Guide

## Problem Summary
The quiz creation is failing with 400 errors because of database schema mismatches between the Java entities and the MySQL database tables.

## Root Cause
The Quiz entity expects certain column names (like `due_date`, `points`, `max_score`, `points_earned`) but the database table either doesn't exist or has different column names.

## ✅ SOLUTION - Follow These Steps Exactly

### Step 1: Fix Database Schema
1. **Open MySQL Workbench or your MySQL client**
2. **Connect to your `elearnhub_db` database**
3. **Run the SQL script**: `UPDATE_QUIZ_DATABASE_SCHEMA.sql`
   ```sql
   -- This script will:
   -- 1. Drop existing quiz tables (if any)
   -- 2. Create new tables with correct column names
   -- 3. Add proper indexes
   -- 4. Insert a test quiz
   ```

### Step 2: Restart Spring Boot Application
1. **Stop your Spring Boot application in Eclipse**
2. **Clean and rebuild the project**
3. **Start the application again**
4. **Check the console for any startup errors**

### Step 3: Test Quiz Creation
1. **Open your frontend at http://localhost:8081**
2. **Login as a teacher**
3. **Go to Classes → Select a class → Create Quiz**
4. **Try creating a simple quiz with:**
   - Title: "Test Quiz"
   - Description: "Testing quiz creation"
   - Duration: 30 minutes
   - Start time: Now
   - End time: Tomorrow
   - Add at least one question

### Step 4: Debug if Still Failing
If you still get 400 errors, check the Eclipse console for detailed error messages.

## ✅ FIXED ISSUES

### 1. Entity Field Mappings
- **Question.marks** → mapped to database column `points`
- **QuizAttempt.totalMarks** → mapped to database column `max_score`
- **QuizAttempt.attemptNumber** → mapped to database column `attempt_number`
- **StudentAnswer.marksAwarded** → mapped to database column `points_earned`

### 2. Database Schema
- Created correct table structure matching entity expectations
- Added missing fields like `time_taken`, `percentage`
- Fixed foreign key relationships

### 3. CORS Configuration
- Already configured in QuizController for ports 8081 and 5173

## ✅ VERIFICATION STEPS

### Check Database Tables
```sql
-- Verify tables exist with correct structure
DESCRIBE quizzes;
DESCRIBE questions;
DESCRIBE quiz_attempts;
DESCRIBE student_answers;

-- Check if test data was inserted
SELECT * FROM quizzes;
SELECT * FROM courses;
```

### Check Spring Boot Logs
Look for these in Eclipse console:
```
✅ SUCCESS: "Quiz created successfully with ID: X"
❌ ERROR: Any SQL exceptions or field mapping errors
```

### Test API Endpoints
You can test these URLs directly:
- GET `http://localhost:8082/quizzes/debug/test` (debug endpoint)
- GET `http://localhost:8082/quizzes/class/15` (get quizzes for class)

## 🚨 IMPORTANT NOTES

1. **Database Password**: Make sure `application.properties` has the correct MySQL password
2. **Course/Class Relationship**: Ensure your classes are properly linked to courses
3. **User Permissions**: Make sure you're logged in as a teacher with proper permissions

## 📋 NEXT STEPS AFTER FIX

1. **Test quiz creation** with different question types
2. **Test quiz publishing** and student access
3. **Test quiz taking** and submission
4. **Verify notifications** are sent to students

## 🔧 IF PROBLEMS PERSIST

1. **Check Eclipse console** for detailed error messages
2. **Verify database connection** is working
3. **Check if all required tables exist** in the database
4. **Ensure proper user roles** and permissions

The quiz system should work perfectly after running the database schema update script and restarting the application.