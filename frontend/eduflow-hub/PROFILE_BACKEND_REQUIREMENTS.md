# Backend Requirements for Profile Page

## Overview
The frontend Profile page now supports role-based profiles (Student, Teacher, Admin). The following backend endpoints are needed.

## Required Endpoints

### 1. Student Profile Endpoint
**Endpoint:** `GET /student/profile`  
**Authorization:** `@PreAuthorize("hasRole('STUDENT')")`  
**Response:** `UserProfile` DTO

**Example Implementation:**
```java
@GetMapping("/profile")
@PreAuthorize("hasRole('STUDENT')")
public ResponseEntity<UserProfile> getStudentProfile(Authentication authentication) {
    String username = authentication.getName();
    User student = userService.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Student not found"));
    
    UserProfile profile = new UserProfile();
    profile.setId(student.getId());
    profile.setUsername(student.getUsername());
    profile.setName(student.getName());
    profile.setEmail(student.getEmail());
    profile.setRole("STUDENT");
    // Add student-specific fields if available
    // profile.setStudentId(...);
    // profile.setMajor(...);
    // profile.setYear(...);
    // profile.setGpa(...);
    // profile.setCreditsCompleted(...);
    // profile.setTotalCredits(...);
    // profile.setAttendanceRate(...);
    
    return ResponseEntity.ok(profile);
}
```

### 2. Admin Profile Endpoint
**Endpoint:** `GET /admin/profile`  
**Authorization:** `@PreAuthorize("hasRole('ADMIN')")`  
**Response:** `UserProfile` DTO

**Example Implementation:**
```java
@GetMapping("/profile")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<UserProfile> getAdminProfile(Authentication authentication) {
    String username = authentication.getName();
    User admin = userService.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Admin not found"));
    
    UserProfile profile = new UserProfile();
    profile.setId(admin.getId());
    profile.setUsername(admin.getUsername());
    profile.setName(admin.getName());
    profile.setEmail(admin.getEmail());
    profile.setRole("ADMIN");
    // Add admin-specific fields if available
    // profile.setAdminLevel(...);
    // profile.setPermissions(...);
    
    return ResponseEntity.ok(profile);
}
```

### 3. Teacher Profile Endpoint (Already Exists)
**Endpoint:** `GET /teacher/profile`  
**Status:** ✅ Should already exist

**Optional Enhancements:**
- Add `department`, `specialization`, `yearsOfExperience`
- Add `totalClasses` (count of classes taught)
- Add `totalStudents` (count of students across all classes)

## UserProfile DTO Structure

```java
public class UserProfile {
    private Long id;
    private String username;
    private String name;
    private String email;
    private String role;
    
    // Common fields
    private String phone;
    private String location;
    private String joinDate;
    
    // Student specific
    private String studentId;
    private String major;
    private String year;
    private String expectedGraduation;
    private Double gpa;
    private Integer creditsCompleted;
    private Integer totalCredits;
    private Double attendanceRate;
    
    // Teacher specific
    private String department;
    private String specialization;
    private Integer yearsOfExperience;
    private Integer totalClasses;
    private Integer totalStudents;
    
    // Admin specific
    private String adminLevel;
    private List<String> permissions;
    
    // Getters and setters...
}
```

## Frontend Behavior

### Fallback Strategy
If a role-specific endpoint doesn't exist, the frontend will:
1. Try the role-specific endpoint first
2. Fall back to `/teacher/profile` if that fails
3. Show error if all attempts fail

### What Works Without Backend Changes
- ✅ Basic profile info (id, username, name, email, role) - if User entity has these fields
- ✅ Role detection from URL path
- ✅ Role-specific UI layout
- ✅ Graceful error handling

### What Needs Backend Support
- ❌ Student-specific fields (GPA, credits, attendance)
- ❌ Teacher-specific stats (total classes, total students)
- ❌ Admin-specific fields (admin level, permissions)
- ❌ Additional fields (phone, location, joinDate)

## Priority

### High Priority (Required for Basic Functionality)
1. ✅ `GET /teacher/profile` - Should already exist
2. ⭐ `GET /student/profile` - Needed for student users
3. ⭐ `GET /admin/profile` - Needed for admin users

### Medium Priority (Enhanced Experience)
4. Add optional fields to UserProfile DTO
5. Calculate and return statistics (total classes, total students for teachers)
6. Add student academic information

### Low Priority (Nice to Have)
7. Profile update endpoints
8. Profile picture upload
9. Additional statistics and analytics

## Testing Checklist

After implementing backend endpoints:

- [ ] Student can view their profile at `/student/profile`
- [ ] Teacher can view their profile at `/teacher/profile`
- [ ] Admin can view their profile at `/admin/profile`
- [ ] Profile shows correct role badge
- [ ] Profile shows user's name, email, username
- [ ] Role-specific fields display correctly (if provided)
- [ ] Error handling works if endpoint doesn't exist
- [ ] Loading states display correctly

