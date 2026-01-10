# Login Password Mismatch Fix

## Problem
User `Namrata112` cannot login after email verification. Error: "Password does not match stored value"

## Root Cause
The password entered during login doesn't match the BCrypt hash stored in the database during registration.

## Solution Options

### Option 1: Use Temporary Password Reset Endpoint (Recommended)

1. **Update SecurityConfig in Eclipse** to allow the temp endpoint:
   ```java
   .requestMatchers("/auth/login", "/auth/register", "/auth/verify-email", "/auth/resend-verification", "/auth/reset-password-temp").permitAll()
   ```

2. **Copy the updated AuthController.java** to Eclipse (it now has a temp password reset endpoint)

3. **Copy the updated UserService.java and UserServiceImpl.java** to Eclipse

4. **Restart your Spring Boot application**

5. **Reset the password using Postman or curl**:
   ```bash
   POST http://localhost:8082/auth/reset-password-temp
   Content-Type: application/json
   
   {
     "username": "Namrata112",
     "newPassword": "123456"
   }
   ```

6. **Try logging in with the new password**: `123456`

### Option 2: Direct Database Update

If you have database access, run this SQL:
```sql
-- Set password to "123456"
UPDATE users SET password = '$2a$10$6iNJ0e9COuLeN0zlhUwGNe4Bh/LWRW.hBzVBRfxmO/VWlFYtXHdAO' 
WHERE username = 'Namrata112';
```

### Option 3: Check What Password Was Used

The user might have used a different password during registration. Common possibilities:
- The name: `Namrata`
- The email prefix: `20230104035`
- A simple password: `password`, `123456`, `admin`

## Testing Steps

1. Reset password using Option 1 or 2
2. Try logging in with username: `Namrata112` and password: `123456`
3. Should successfully login and redirect to teacher dashboard

## Files Updated
- `AuthController.java` - Added temp password reset endpoint
- `UserService.java` - Added simple updateUser method
- `UserServiceImpl.java` - Implemented simple updateUser method

## After Login Works
Once login is working, you can remove the temporary password reset endpoint for security.