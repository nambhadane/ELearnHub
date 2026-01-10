# Database Setup Required - Fix for 500 Errors

## Problem
After successful login, the application shows multiple 500 errors because database tables are missing:
- `Table 'elearn_teacher.courses' doesn't exist`
- `/api/courses` returns 500 error
- `/api/assignments/my-assignments` returns 404 error

## Root Cause
The database schema is incomplete. Essential tables are missing.

## Solution

### Step 1: Create Database Tables
Run the SQL script `CREATE_ESSENTIAL_TABLES.sql` in your MySQL database:

```sql
-- Connect to your MySQL database
mysql -u root -p elearn_teacher

-- Or use MySQL Workbench/phpMyAdmin and run the script
```

The script creates:
- ✅ `users` table (with proper structure)
- ✅ `courses` table
- ✅ `class_entity` table
- ✅ `class_student` table (many-to-many)
- ✅ `assignments` table
- ✅ `email_verification_tokens` table
- ✅ `system_settings` table
- ✅ `notifications` table
- ✅ Sample data and indexes

### Step 2: Copy Updated Controllers to Eclipse
Copy these new/updated files to your Eclipse project:

1. **`CourseController.java`** - NEW: Handles `/api/courses` endpoints
2. **`AssignmentController.java`** - UPDATED: Added `/my-assignments` endpoint

### Step 3: Update SecurityConfig in Eclipse
Make sure your SecurityConfig allows these endpoints:

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/auth/login", "/auth/register", "/auth/verify-email", "/auth/resend-verification", "/auth/reset-password-temp", "/auth/test-password").permitAll()
    .requestMatchers("/courses/**").hasAnyRole("TEACHER", "STUDENT", "ADMIN")
    .requestMatchers("/assignments/**").hasAnyRole("TEACHER", "STUDENT", "ADMIN")
    .requestMatchers("/admin/**").hasRole("ADMIN")
    .requestMatchers("/teacher/**").hasRole("TEACHER")
    .requestMatchers("/student/**").hasRole("STUDENT")
    .requestMatchers("/actuator/**").permitAll()
    .requestMatchers("/error").permitAll()
    .anyRequest().authenticated()
)
```

### Step 4: Restart Spring Boot Application
Restart your application in Eclipse after:
1. Running the SQL script
2. Adding the new controllers
3. Updating SecurityConfig

### Step 5: Test the Fixed Endpoints

1. **Test courses endpoint**:
   ```bash
   GET http://localhost:8082/courses
   Authorization: Bearer YOUR_JWT_TOKEN
   ```

2. **Test my-assignments endpoint**:
   ```bash
   GET http://localhost:8082/assignments/my-assignments
   Authorization: Bearer YOUR_JWT_TOKEN
   ```

## Expected Results After Fix

✅ **Teacher Dashboard**: Should load without 500 errors
✅ **Courses**: Should return empty array `[]` instead of 500 error
✅ **Assignments**: Should return empty array `[]` instead of 404 error
✅ **Student Dashboard**: Should load properly

## Sample Admin User
The SQL script creates a default admin user:
- **Username**: `admin`
- **Password**: `admin123`
- **Email**: `admin@eduflow.com`
- **Role**: `ADMIN`

## Next Steps After Database Setup
1. Create some sample courses through admin panel
2. Create some classes and assign students
3. Create assignments for testing
4. Test the complete workflow

## Files Created/Updated
- `CREATE_ESSENTIAL_TABLES.sql` - Database schema
- `CourseController.java` - NEW controller
- `AssignmentController.java` - Updated with my-assignments endpoint