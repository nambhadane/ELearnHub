# Registration Password Debug Guide

## Problem
Users can register and verify email, but cannot login with the same password they used during registration.

## Debug Steps

### 1. Update SecurityConfig in Eclipse
Add the test endpoint to permitAll:
```java
.requestMatchers("/auth/login", "/auth/register", "/auth/verify-email", "/auth/resend-verification", "/auth/reset-password-temp", "/auth/test-password").permitAll()
```

### 2. Copy Updated Files to Eclipse
- `AuthController.java` (now has debug endpoint)
- `UserServiceImpl.java` (now has debug logging)

### 3. Restart Spring Boot Application

### 4. Test Registration Flow

1. **Register a new user** with a simple password:
   ```bash
   POST http://localhost:8082/auth/register
   Content-Type: application/json
   
   {
     "username": "testuser123",
     "name": "Test User",
     "email": "test@example.com",
     "password": "simple123",
     "role": "TEACHER"
   }
   ```

2. **Check the console logs** for password encoding debug info:
   ```
   🔐 REGISTRATION DEBUG:
   🔐 Original password length: 9
   🔐 Encoded password: $2a$10$...
   🔐 Encoded password length: 60
   
   🔐 CREATEUSER DEBUG:
   🔐 Password before save: $2a$10$...
   🔐 Password length before save: 60
   🔐 Password after save: $2a$10$...
   🔐 Password length after save: 60
   ```

3. **Verify email** (if required)

4. **Test password matching**:
   ```bash
   POST http://localhost:8082/auth/test-password
   Content-Type: application/json
   
   {
     "username": "testuser123",
     "password": "simple123"
   }
   ```

   Expected response:
   ```json
   {
     "username": "testuser123",
     "passwordProvided": "simple123",
     "storedPasswordPrefix": "$2a$10$...",
     "storedPasswordLength": 60,
     "encodedTestPassword": "$2a$10$...",
     "passwordMatches": true,
     "isBCryptFormat": true
   }
   ```

5. **Try login**:
   ```bash
   POST http://localhost:8082/auth/login
   Content-Type: application/json
   
   {
     "username": "testuser123",
     "password": "simple123"
   }
   ```

### 5. Analyze Results

- If `passwordMatches` is `false`, there's an encoding issue
- If `passwordMatches` is `true` but login fails, there's a login logic issue
- Check console logs for any password modification during save

### 6. Common Issues to Check

1. **Double encoding**: Password encoded twice
2. **Password modification**: Something modifying password after encoding
3. **Character encoding**: Special characters in password
4. **Database constraints**: Password field too short in database

### 7. Quick Fix if Issue Found

If password encoding is working but login fails, the issue might be in the login authentication logic. Check if:
- Email verification is blocking login
- Role-based access is blocking login
- JWT token generation is failing

## Expected Outcome
This debug process should reveal exactly where the password encoding/matching is failing.