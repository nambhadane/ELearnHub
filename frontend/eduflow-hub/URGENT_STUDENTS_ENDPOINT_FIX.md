# URGENT: Fix Missing /students Endpoint

## Problem
`GET /students` returns 404 because the endpoint doesn't exist. Spring is trying to find a static resource instead of a controller endpoint.

## Quick Fix

Add this endpoint to your **StudentController** (or create a new controller if StudentController doesn't exist):

```java
package com.elearnhub.teacher_service.Controller;

import com.elearnhub.teacher_service.dto.ParticipantDTO;
import com.elearnhub.teacher_service.entity.User;
import com.elearnhub.teacher_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/student")
public class StudentController {
    
    @Autowired
    private UserService userService;
    
    // ✅ ADD THIS ENDPOINT - Get all students (for teachers to add to classes)
    @GetMapping("/students")  // This will be /student/students
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> getAllStudents(Authentication authentication) {
        try {
            String username = authentication.getName();
            User teacher = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Teacher not found"));

            // Get all students (users with role STUDENT)
            List<ParticipantDTO> students = userService.getAllStudents();
            
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to fetch students: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // ... your other student endpoints ...
}
```

**OR if you want it at root level `/students` (not `/student/students`), create a separate controller:**

```java
package com.elearnhub.teacher_service.Controller;

import com.elearnhub.teacher_service.dto.ParticipantDTO;
import com.elearnhub.teacher_service.entity.User;
import com.elearnhub.teacher_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")  // or just "/" if you don't have /api prefix
public class StudentListController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/students")  // This will be /api/students or /students
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> getAllStudents(Authentication authentication) {
        try {
            String username = authentication.getName();
            User teacher = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Teacher not found"));

            List<ParticipantDTO> students = userService.getAllStudents();
            
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to fetch students: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
```

---

## Required Service Method

Make sure `UserService` has this method:

```java
List<ParticipantDTO> getAllStudents();
```

---

## Required Service Implementation

Add this to `UserServiceImpl` (or `UserService` if it's a class):

```java
@Transactional(readOnly = true)
public List<ParticipantDTO> getAllStudents() {
    List<User> students = userRepository.findByRole("STUDENT");

    return students.stream()
            .map(student -> {
                ParticipantDTO dto = new ParticipantDTO();
                dto.setId(student.getId());
                dto.setName(student.getName());
                dto.setUsername(student.getUsername());
                dto.setRole(student.getRole());
                // Add avatar if you have it
                // dto.setAvatar(student.getProfilePicture());
                return dto;
            })
            .collect(Collectors.toList());
}
```

**Add import:**
```java
import java.util.stream.Collectors;
```

---

## Required Repository Method

Make sure `UserRepository` has:

```java
List<User> findByRole(String role);
```

---

## Update Frontend API (if needed)

If you add the endpoint to `StudentController` at `/student/students`, update the frontend:

**In `api.ts`, change:**
```typescript
export async function getAllStudents(): Promise<ParticipantDTO[]> {
  const response = await fetch(`${API_BASE_URL}/students`, {
```

**To:**
```typescript
export async function getAllStudents(): Promise<ParticipantDTO[]> {
  const response = await fetch(`${API_BASE_URL}/student/students`, {
```

**OR if you create it at root `/students` or `/api/students`, keep it as is.**

---

## Quick Test

After adding the endpoint, test:
1. Restart your Spring Boot application
2. Try `GET /students` (or `/student/students` depending on where you added it)
3. Should return a list of students in JSON format

---

## Summary

1. ✅ Add `getAllStudents()` method to `UserService`
2. ✅ Implement it in `UserServiceImpl` (or `UserService`)
3. ✅ Add `findByRole(String role)` to `UserRepository` (if not exists)
4. ✅ Add `GET /students` endpoint to a controller (StudentController or new controller)
5. ✅ Update frontend API URL if endpoint location changes

The frontend is already calling `/students`, so make sure the endpoint matches that path!






