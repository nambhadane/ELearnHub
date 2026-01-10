# Registration Issue Debug Guide

## Problem
Users can register successfully (see "Registration successful" message), but the user data is not visible in the database and login fails.

## Observations from Logs

1. **Registration appears successful:**
   - ✅ User saved with ID: 6 Role: TEACHER
   - ✅ User saved with ID: 7 Role: STUDENT
   - Hibernate INSERT query executes
   - Transaction commits successfully
   - Returns 201 CREATED

2. **Login fails:**
   - Failed to find user 'nikita@gmail.com'
   - Returns 401 UNAUTHORIZED

## Possible Causes

### 1. Database Transaction/Connection Issue
- The transaction commits but data isn't actually persisted
- Check if using an in-memory database that resets
- Verify database connection is persistent

### 2. Username/Email Mismatch
- User registers with username: `nikita`
- User tries to login with username: `nikita@gmail.com`
- Backend searches by username field, which doesn't match

### 3. Database Configuration Issue
- Check `application.properties` or `application.yml`
- Verify database URL, username, password
- Check if `spring.jpa.hibernate.ddl-auto` is set correctly
- Ensure `spring.jpa.properties.hibernate.format_sql` is enabled for better debugging

### 4. Multiple Database Connections
- Registration might be saving to one database
- Login might be reading from another database
- Check database URLs match

## Frontend Code Status

✅ Frontend is correctly:
- Sending registration request with username, email, password, role
- Sending login request with username and password
- Handling responses appropriately

## Debugging Steps for Backend

### 1. Check Database Configuration
Look for `application.properties` or `application.yml` in your Spring Boot backend:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/your_database
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### 2. Verify User Service Implementation
Check if `createUser` method is properly annotated with `@Transactional`:
```java
@Transactional
public User createUser(User user) {
    return userRepository.save(user);
}
```

### 3. Check Database Directly
After registration, directly query the database:
```sql
SELECT * FROM users;
```
See if the user actually exists.

### 4. Enable More Detailed Logging
Add to `application.properties`:
```properties
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
logging.level.org.springframework.orm.jpa=DEBUG
logging.level.org.springframework.transaction=DEBUG
```

### 5. Verify Repository Save
Check if `UserRepository.save()` is returning the saved entity with an ID.

## Frontend Changes Made

I've added console logging to help debug:
- Registration request/response logged
- Login request/response logged

Check browser console (F12) to see what's being sent and received.

## Next Steps

1. **Verify Database**: Query the database directly after registration to see if data exists
2. **Check Username**: Ensure the username used for registration matches the username used for login
3. **Review Backend Logs**: Look for any transaction rollback messages or database errors
4. **Test with Database Client**: Use MySQL Workbench, DBeaver, or similar tool to check the database directly

## Note

This is primarily a **backend/database issue**, not a frontend issue. The frontend is sending the correct data, and the backend is accepting it, but the data isn't persisting or isn't being found during login.

