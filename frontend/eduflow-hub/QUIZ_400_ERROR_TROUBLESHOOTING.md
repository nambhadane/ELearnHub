# Quiz 400 Error Troubleshooting Guide

## Current Issues
1. `GET /api/quizzes/class/15` returns 400 Bad Request
2. `POST /api/quizzes` returns 400 Bad Request

## Root Causes & Fixes

### Issue 1: Database Tables Missing
The quiz tables might not exist in your database.

**Solution**: Run the complete SQL script:
```sql
-- Run CREATE_ESSENTIAL_TABLES.sql in your MySQL database
-- This creates all required tables including:
-- - quizzes
-- - questions  
-- - question_options
-- - quiz_attempts
-- - student_answers
```

### Issue 2: Class-Course Association Problem
The `getQuizzesByClass` method was incorrectly using classId as courseId.

**Fixed in**: `QuizServiceImpl.java` - Now properly gets courseId from classId

### Issue 3: Missing Debug Information
Added comprehensive logging to identify issues.

## Troubleshooting Steps

### Step 1: Copy Updated Files to Eclipse
Copy these updated files:
- `QuizController.java` (added debug logging + test endpoint)
- `QuizServiceImpl.java` (fixed class-to-course mapping)

### Step 2: Restart Spring Boot Application

### Step 3: Test Database Connection
Call the debug endpoint to check database state:
```bash
GET http://localhost:8082/quizzes/debug/test
Authorization: Bearer YOUR_JWT_TOKEN
```

Expected response:
```json
{
  "totalClasses": 5,
  "totalQuizzes": 0,
  "class15Exists": true,
  "class15Name": "Math Class",
  "class15CourseId": 1
}
```

### Step 4: Check Console Logs
When testing quiz operations, check Eclipse console for:

**For GET /quizzes/class/15**:
```
🎯 Getting quizzes for class ID: 15
✅ Found 0 quizzes for class 15
```

**For POST /quizzes**:
```
🎯 Creating quiz: Quiz Title
🎯 Class ID: 15
🎯 Start Time: 2024-12-15T10:00:00
🎯 End Time: 2024-12-15T11:00:00
✅ Quiz created successfully with ID: 1
```

### Step 5: Common Error Messages & Solutions

#### Error: "Class not found"
**Cause**: Class ID 15 doesn't exist
**Solution**: 
```sql
SELECT * FROM class_entity WHERE id = 15;
-- If not found, create a class or use existing class ID
```

#### Error: "Table 'elearn_teacher.quizzes' doesn't exist"
**Cause**: Database tables not created
**Solution**: Run the complete SQL script

#### Error: "Class must be associated with a course"
**Cause**: Class has no course assigned
**Solution**:
```sql
-- Check class-course association
SELECT c.id, c.name, c.course_id FROM class_entity c WHERE id = 15;

-- If course_id is NULL, assign a course
UPDATE class_entity SET course_id = 1 WHERE id = 15;
```

#### Error: "Access Denied" or 403
**Cause**: Security configuration issue
**Solution**: Update SecurityConfig to allow `/quizzes/**`

### Step 6: Manual Database Verification

1. **Check if tables exist**:
```sql
SHOW TABLES LIKE '%quiz%';
-- Should show: quizzes, quiz_attempts, questions, question_options, student_answers
```

2. **Check class data**:
```sql
SELECT c.id, c.name, c.course_id, co.name as course_name 
FROM class_entity c 
LEFT JOIN courses co ON c.course_id = co.id 
WHERE c.id = 15;
```

3. **Check quiz data**:
```sql
SELECT * FROM quizzes WHERE course_id = 1;
```

## Expected Results After Fix

✅ **GET /quizzes/class/15**: Returns empty array `[]` (no 400 error)
✅ **POST /quizzes**: Creates quiz successfully
✅ **Debug logs**: Show detailed information in console
✅ **Database**: Quiz data saved correctly

## Test Quiz Creation Payload

Use this JSON to test quiz creation:
```json
{
  "classId": 15,
  "title": "Test Quiz",
  "description": "This is a test quiz",
  "startTime": "2024-12-16T10:00:00",
  "endTime": "2024-12-16T11:00:00",
  "duration": 60,
  "totalMarks": 100,
  "passingMarks": 50,
  "maxAttempts": 3,
  "randomizeQuestions": false,
  "showResultsImmediately": true,
  "status": "DRAFT",
  "questions": []
}
```

## Files Updated
- `QuizController.java` - Added debug logging and test endpoint
- `QuizServiceImpl.java` - Fixed class-to-course ID mapping
- `CREATE_ESSENTIAL_TABLES.sql` - Complete database schema