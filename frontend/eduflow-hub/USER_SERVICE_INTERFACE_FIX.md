# UserService Interface Fix

## Problem
`UserService` is currently a **class**, but `UserServiceImpl` is trying to implement it as an **interface**. This causes the error: "The type UserService cannot be a superinterface of UserServiceImpl; a superinterface must be an interface"

## Solution
Create a `UserService` **interface** and rename the current `UserService` class to `UserServiceImpl`.

---

## 1. UserService Interface (NEW - Create this file)

```java
package com.elearnhub.teacher_service.service;

import com.elearnhub.teacher_service.dto.ParticipantDTO;
import com.elearnhub.teacher_service.dto.UserDTO;
import com.elearnhub.teacher_service.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    
    User createUser(User user);
    
    Optional<User> getUserById(Long id);
    
    List<UserDTO> getAllUsers();
    
    User updateUser(Long id, User user);
    
    void deleteUser(Long id);
    
    boolean resetPassword(String username, String newPlainPassword);
    
    boolean existsByUsername(String username);
    
    Optional<User> findByUsername(String username);
    
    List<ParticipantDTO> getAllTeachers();
}
```

---

## 2. UserServiceImpl (RENAME your current UserService class to this)

```java
package com.elearnhub.teacher_service.service;

import com.elearnhub.teacher_service.dto.ParticipantDTO;
import com.elearnhub.teacher_service.dto.UserDTO;
import com.elearnhub.teacher_service.entity.User;
import com.elearnhub.teacher_service.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService, UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User createUser(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists: " + user.getUsername());
        }

        // ✅ Password is already encoded, just save it as-is
        User savedUser = userRepository.save(user);
        System.out.println("✅ User saved with ID: " + savedUser.getId() + " Role: " + savedUser.getRole());
        return savedUser;
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> new UserDTO(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getRole()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public User updateUser(Long id, User user) {
        Optional<User> existingUser = userRepository.findById(id);
        if (existingUser.isPresent()) {
            User updatedUser = existingUser.get();

            // Preserve username and role - these should NEVER change via updateUser
            String originalUsername = updatedUser.getUsername();
            String originalRole = updatedUser.getRole();

            // Only update email if provided (not null)
            if (user.getEmail() != null) {
                updatedUser.setEmail(user.getEmail());
            }
            // Only update name if provided (not null)
            if (user.getName() != null) {
                updatedUser.setName(user.getName());
            }
            // Only update password if provided and not empty
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                updatedUser.setPassword(passwordEncoder.encode(user.getPassword()));
            }

            // Explicitly preserve username and role (prevent accidental changes)
            updatedUser.setUsername(originalUsername);
            updatedUser.setRole(originalRole);

            return userRepository.save(updatedUser);
        }
        throw new RuntimeException("User not found with id: " + id);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Debug: Log password format to verify encoding
        String passwordPrefix = user.getPassword() != null && user.getPassword().length() > 10
            ? user.getPassword().substring(0, Math.min(30, user.getPassword().length()))
            : "null or too short";

        System.out.println("🔍 Authenticated user: " + user.getUsername() + " | Role: " + user.getRole());
        System.out.println("🔐 Password prefix in DB: " + passwordPrefix + "...");
        System.out.println("🔐 Password length: " + (user.getPassword() != null ? user.getPassword().length() : 0));
        System.out.println("🔐 Is BCrypt format: " + (user.getPassword() != null && user.getPassword().startsWith("$2")));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
    }

    @Override
    public boolean resetPassword(String username, String newPlainPassword) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            return false;
        }
        User user = userOptional.get();
        String encodedPassword = passwordEncoder.encode(newPlainPassword);
        user.setPassword(encodedPassword);
        userRepository.save(user);
        System.out.println("✅ Password reset for user: " + username);
        System.out.println("🔐 New encoded password: " + encodedPassword.substring(0, Math.min(30, encodedPassword.length())) + "...");
        return true;
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<ParticipantDTO> getAllTeachers() {
        List<User> teachers = userRepository.findByRole("TEACHER");

        return teachers.stream()
                .map(teacher -> {
                    ParticipantDTO dto = new ParticipantDTO();
                    dto.setId(teacher.getId());
                    dto.setName(teacher.getName());
                    dto.setUsername(teacher.getUsername());
                    dto.setRole(teacher.getRole());
                    // Add avatar if you have it in User entity
                    // dto.setAvatar(teacher.getProfilePicture());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
```

---

## Steps to Fix

### Option 1: Create Interface and Rename Class (RECOMMENDED)

1. **Create new file**: `UserService.java` (interface) - Copy the interface code above
2. **Rename your current file**: `UserService.java` → `UserServiceImpl.java`
3. **Update the class declaration** in `UserServiceImpl.java`:
   - Change: `public class UserService implements UserDetailsService`
   - To: `public class UserServiceImpl implements UserService, UserDetailsService`
4. **Add `@Override` annotations** to all methods that implement the interface
5. **Update all imports** in other files that use `UserService` (they should still work since Spring will inject `UserServiceImpl` when `UserService` is requested)

### Option 2: Keep Current Structure (If you don't want to create interface)

If you prefer to keep `UserService` as a class and not create an interface:

1. **Delete** `UserServiceImpl.java` (the file you just created)
2. **Add the `getAllTeachers()` method** directly to your existing `UserService` class:

```java
// Add this method to your existing UserService class
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public List<ParticipantDTO> getAllTeachers() {
    List<User> teachers = userRepository.findByRole("TEACHER");

    return teachers.stream()
            .map(teacher -> {
                ParticipantDTO dto = new ParticipantDTO();
                dto.setId(teacher.getId());
                dto.setName(teacher.getName());
                dto.setUsername(teacher.getUsername());
                dto.setRole(teacher.getRole());
                return dto;
            })
            .collect(Collectors.toList());
}
```

---

## Important Notes

1. **Spring Dependency Injection**: If you create the interface, Spring will automatically inject `UserServiceImpl` when you `@Autowired UserService` in other classes. This is the recommended pattern.

2. **UserDetailsService**: `UserServiceImpl` implements both `UserService` (your business interface) and `UserDetailsService` (Spring Security interface). This is correct.

3. **Transaction Annotations**: 
   - `@Transactional` on class level uses `jakarta.transaction.Transactional`
   - `@Transactional(readOnly = true)` on method uses `org.springframework.transaction.annotation.Transactional`
   - This is fine - Spring will handle both

4. **Repository Method**: Make sure `UserRepository` has:
   ```java
   List<User> findByRole(String role);
   ```

---

## Quick Fix (If you just want to add the method)

If you want the quickest fix without creating an interface, just add this method to your existing `UserService` class:

```java
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public List<ParticipantDTO> getAllTeachers() {
    List<User> teachers = userRepository.findByRole("TEACHER");

    return teachers.stream()
            .map(teacher -> {
                ParticipantDTO dto = new ParticipantDTO();
                dto.setId(teacher.getId());
                dto.setName(teacher.getName());
                dto.setUsername(teacher.getUsername());
                dto.setRole(teacher.getRole());
                return dto;
            })
            .collect(Collectors.toList());
}
```

And make sure to import:
```java
import com.elearnhub.teacher_service.dto.ParticipantDTO;
import java.util.stream.Collectors;
```

Then delete the `UserServiceImpl.java` file you created.

---

## Recommended Approach

I recommend **Option 1** (creating the interface) because:
- ✅ Follows Spring best practices
- ✅ Better for testing (can mock the interface)
- ✅ Cleaner separation of concerns
- ✅ More maintainable

But if you want the quickest fix, use **Option 2** (just add the method to your existing class).

