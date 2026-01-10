# Backend Debug Checklist - Registration Issue

## Problem
Users can register successfully (see 201 CREATED response), but user data is not visible in database and login fails.

## Database Connection Check

Your configuration shows:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/elearn_teacher
spring.datasource.username=root
spring.datasource.password=Namrata@1805
spring.jpa.hibernate.ddl-auto=update
```

### Step 1: Check Database Directly
1. Open MySQL Workbench or command line
2. Connect to your MySQL server
3. Run:
```sql
USE elearn_teacher;
SELECT * FROM users;
```
4. **Check if the registered users appear in the database**

### Step 2: Check UserService Implementation

Make sure your `UserService.createUser()` method is properly annotated:

```java
@Service
@Transactional  // ✅ Make sure this is present
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Transactional  // ✅ Make sure method has this annotation
    public User createUser(User user) {
        // Encode password
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        
        // Save user
        User savedUser = userRepository.save(user);
        
        // ✅ Force flush to database
        userRepository.flush();
        
        // ✅ Verify user was saved
        System.out.println("✅ User saved with ID: " + savedUser.getId());
        
        return savedUser;
    }
}
```

### Step 3: Check Repository Save Method

Verify your `UserRepository` interface:

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}
```

### Step 4: Check for Transaction Rollback

Add more logging to see if transaction is being rolled back:

```java
@Transactional
public User createUser(User user) {
    try {
        // Your existing code...
        User savedUser = userRepository.save(user);
        
        // Force immediate flush
        userRepository.flush();
        
        // Verify it was saved
        Optional<User> verify = userRepository.findById(savedUser.getId());
        if (verify.isPresent()) {
            System.out.println("✅ VERIFIED: User exists in database with ID: " + savedUser.getId());
        } else {
            System.out.println("❌ ERROR: User was NOT saved to database!");
        }
        
        return savedUser;
    } catch (Exception e) {
        System.err.println("❌ ERROR saving user: " + e.getMessage());
        e.printStackTrace();
        throw e; // Re-throw to trigger rollback
    }
}
```

### Step 5: Check Database Table Structure

Verify the `users` table structure matches your User entity:

```sql
DESCRIBE users;
```

Make sure all required fields (username, password, email, role) exist.

### Step 6: Check for Multiple Database Connections

Ensure you're not accidentally connecting to different databases:
- Check if there are multiple MySQL instances running
- Verify the database name `elearn_teacher` exists
- Check for connection pooling issues

### Step 7: Enable Detailed Hibernate Logging

Add to `application.properties`:

```properties
# Detailed Hibernate logging
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
logging.level.org.hibernate.engine.transaction=DEBUG
logging.level.org.springframework.orm.jpa=DEBUG
logging.level.org.springframework.transaction=DEBUG

# Check if transactions are being committed
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.use_sql_comments=true
```

### Step 8: Username vs Email Issue

**CRITICAL**: The login is searching by `username` field, not email!

If user registered with:
- Username: `nikita`
- Email: `nikita@gmail.com`

Then login must use: `nikita` (NOT `nikita@gmail.com`)

Check your registration logs - what username was actually saved?

### Step 9: Check AuthController Register Method

Make sure the register method is committing properly:

```java
@PostMapping("/register")
public ResponseEntity<?> register(@RequestBody User registerRequest) {
    try {
        // ... validation ...
        
        // Encode password
        String encodedPassword = passwordEncoder.encode(registerRequest.getPassword());
        registerRequest.setPassword(encodedPassword);
        
        // Save user - this should be in a transaction
        User savedUser = userService.createUser(registerRequest);
        
        // ✅ Verify immediately
        Optional<User> verify = userService.findByUsername(savedUser.getUsername());
        if (verify.isPresent()) {
            System.out.println("✅ VERIFIED: User can be retrieved after save");
        }
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Registration successful");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
        
    } catch (Exception e) {
        e.printStackTrace(); // Check for exceptions
        // ... error handling ...
    }
}
```

### Step 10: Check MySQL Auto-Commit

Verify MySQL auto-commit is enabled (it should be by default):

```sql
SHOW VARIABLES LIKE 'autocommit';
```

Should show `autocommit = ON`

## Most Likely Issues:

1. **Transaction not committing** - Check `@Transactional` annotations
2. **Username mismatch** - User registers with one username but tries to login with email
3. **Database connection issue** - Multiple database connections or wrong database
4. **Entity not being flushed** - Add `repository.flush()` after save

## Quick Test:

After registration, immediately query the database:
```sql
SELECT * FROM users WHERE username = 'YOUR_REGISTERED_USERNAME';
```

If the user doesn't exist, the transaction is not committing properly.

