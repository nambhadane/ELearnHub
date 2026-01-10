# Quiz Creation 400 Error Fix

## Problem
When trying to create a quiz through the teacher dashboard, it shows "Failed to create quiz" with a 400 Bad Request error.

## Root Causes
1. **Missing CORS configuration** in QuizController
2. **Field mapping issue**: Frontend sends `classId` but Quiz entity expects `courseId`
3. **Missing database tables** for quizzes
4. **Validation errors** in quiz creation logic

## Solution

### Step 1: Update Database Schema
Run the updated `CREATE_ESSENTIAL_TABLES.sql` script which now includes:
- ✅ `quizzes` table
- ✅ `questions` table  
- ✅ `question_options` table
- ✅ `quiz_attempts` table
- ✅ `student_answers` table

### Step 2: Copy Updated Files to Eclipse
Copy these updated files to your Eclipse project:

1. **`QuizController.java`** - Added CORS and debug logging
2. **`QuizServiceImpl.java`** - Fixed courseId mapping issue
3. **`CREATE_ESSENTIAL_TABLES.sql`** - Added quiz tables

### Step 3: Update SecurityConfig in Eclipse
Make sure your SecurityConfig allows quiz endpoints:

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/auth/login", "/auth/register", "/auth/verify-email", "/auth/resend-verification", "/auth/reset-password-temp", "/auth/test-password").permitAll()
    .requestMatchers("/courses/**").hasAnyRole("TEACHER", "STUDENT", "ADMIN")
    .requestMatchers("/assignments/**").hasAnyRole("TEACHER", "STUDENT", "ADMIN")
    .requestMatchers("/quizzes/**").hasAnyRole("TEACHER", "STUDENT", "ADMIN")
    .requestMatchers("/admin/**").hasRole("ADMIN")
    .requestMatchers("/teacher/**").hasRole("TEACHER")
    .requestMatchers("/student/**").hasRole("STUDENT")
    .requestMatchers("/actuator/**").permitAll()
    .requestMatchers("/error").permitAll()
    .anyRequest().authenticated()
)
```

### Step 4: Restart Spring Boot Application
Restart your application in Eclipse after making all changes.

### Step 5: Test Quiz Creation

1. **Check console logs** when creating a quiz - you should see:
   ```
   🎯 Creating quiz: [Quiz Title]
   🎯 Class ID: [Class ID]
   🎯 Start Time: [Start Time]
   🎯 End Time: [End Time]
   ✅ Quiz created successfully with ID: [Quiz ID]
   ```

2. **If errors occur**, check for:
   - Missing course association in the class
   - Invalid date formats
   - Missing required fields

### Step 6: Common Issues and Solutions

#### Issue 1: "Class must be associated with a course"
**Solution**: Make sure the class has a valid course assigned:
```sql
-- Check class-course associations
SELECT c.id, c.name, c.course_id, co.name as course_name 
FROM class_entity c 
LEFT JOIN courses co ON c.course_id = co.id;

-- Update class with course if missing
UPDATE class_entity SET course_id = 1 WHERE id = [CLASS_ID];
```

#### Issue 2: "Class not found"
**Solution**: Verify the class exists:
```sql
SELECT * FROM class_entity WHERE id = [CLASS_ID];
```

#### Issue 3: Database table errors
**Solution**: Run the complete SQL script to create all tables.

## Expected Results After Fix

✅ **Quiz Creation**: Should work without 400 errors
✅ **Debug Logs**: Should show quiz creation process in console
✅ **Database**: Quiz should be saved in `quizzes` table
✅ **Frontend**: Should show success message and redirect

## Testing Steps

1. **Create a course** (if not exists)
2. **Create a class** and assign it to the course
3. **Try creating a quiz** for that class
4. **Check console logs** for debug information
5. **Verify in database** that quiz was created

## Files Updated
- `QuizController.java` - Added CORS and debug logging
- `QuizServiceImpl.java` - Fixed courseId mapping
- `CREATE_ESSENTIAL_TABLES.sql` - Added quiz database tables