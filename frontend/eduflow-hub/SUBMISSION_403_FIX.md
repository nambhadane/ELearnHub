# 🔧 Fix for Assignment Submission 403 Forbidden Error

## Problem

When uploading files for assignment submission, you get:
- **Error**: `POST http://localhost:8081/api/assignments/submissions 403 (Forbidden)`
- **Backend Log**: `Set SecurityContextHolder to anonymous SecurityContext`

The JWT token is not being recognized for multipart/form-data requests.

## Root Cause

Spring Security's JWT filter might not be processing the `Authorization` header correctly for multipart requests, or the endpoint path/security configuration is incorrect.

## Solution

### Fix 1: Verify Endpoint Path and Mapping

Make sure your `AssignmentController` has the correct endpoint:

```java
@RestController
@RequestMapping("/assignments")
public class AssignmentController {
    
    // ... other methods ...
    
    @PostMapping(value = "/submissions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> saveSubmission(
            @RequestParam Long assignmentId,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) List<MultipartFile> files,
            Authentication authentication) {
        try {
            // Get authenticated student
            String username = authentication.getName();
            User student = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            // Validate assignmentId is provided
            if (assignmentId == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Assignment ID is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(error);
            }

            // Create submission DTO
            SubmissionDTO submissionDTO = new SubmissionDTO();
            submissionDTO.setAssignmentId(assignmentId);
            submissionDTO.setContent(content);
            submissionDTO.setStudentId(student.getId()); // ✅ Set from authenticated user

            // Handle file uploads if provided
            if (files != null && !files.isEmpty()) {
                try {
                    List<String> filePaths = new ArrayList<>();
                    for (MultipartFile file : files) {
                        if (!file.isEmpty()) {
                            // Generate unique filename
                            String originalFilename = file.getOriginalFilename();
                            String timestamp = String.valueOf(System.currentTimeMillis());
                            String filename = timestamp + "_" + originalFilename;

                            // Save file
                            String uploadDir = "uploads/submissions/";
                            File uploadDirectory = new File(uploadDir);
                            if (!uploadDirectory.exists()) {
                                uploadDirectory.mkdirs();
                            }

                            String filePath = uploadDir + filename;
                            File destFile = new File(filePath);
                            file.transferTo(destFile);

                            filePaths.add(filePath);
                        }
                    }

                    // Store multiple file paths as comma-separated string
                    if (!filePaths.isEmpty()) {
                        submissionDTO.setFilePath(String.join(",", filePaths));
                    }

                } catch (IOException e) {
                    Map<String, String> error = new HashMap<>();
                    error.put("message", "Failed to save file: " + e.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(error);
                }
            }

            // Save submission
            SubmissionDTO savedSubmission = assignmentService.saveSubmission(submissionDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(savedSubmission);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to save submission: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(error);
        }
    }
}
```

### Fix 2: Ensure JWT Filter Processes Multipart Requests

Check your `JwtFilter` to ensure it processes multipart requests. The filter should run **before** the multipart resolver:

```java
@Component
public class JwtFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request, 
            HttpServletResponse response, 
            FilterChain chain) throws ServletException, IOException {
        
        // ✅ IMPORTANT: Extract token from header BEFORE multipart processing
        String header = request.getHeader("Authorization");
        
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            String username = jwtUtil.extractUsername(token);
            
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                if (jwtUtil.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }
        
        chain.doFilter(request, response);
    }
}
```

### Fix 3: Update Security Configuration

Make sure your `SecurityConfig` allows multipart requests and the JWT filter runs early:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Autowired
    private JwtFilter jwtFilter;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/assignments/submissions").hasRole("STUDENT") // ✅ Explicitly allow
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class); // ✅ JWT filter before authentication
            
        return http.build();
    }
}
```

### Fix 4: Verify Frontend Request Format

The frontend code looks correct, but verify the endpoint path matches:

```typescript
// In api.ts - this should match backend endpoint
const response = await fetch(`${API_BASE_URL}/assignments/submissions`, {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`, // ✅ Token is sent
  },
  body: formData, // ✅ FormData with files
});
```

**Important**: Don't set `Content-Type` header manually for FormData - the browser will set it automatically with the boundary.

### Fix 5: Alternative - Use @RequestPart Instead of @RequestParam

If `@RequestParam` doesn't work, try using `@RequestPart`:

```java
@PostMapping(value = "/submissions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
@PreAuthorize("hasRole('STUDENT')")
public ResponseEntity<?> saveSubmission(
        @RequestPart("assignmentId") String assignmentIdStr,
        @RequestPart(value = "content", required = false) String content,
        @RequestPart(value = "files", required = false) List<MultipartFile> files,
        Authentication authentication) {
    try {
        Long assignmentId = Long.parseLong(assignmentIdStr);
        // ... rest of the code
    } catch (Exception e) {
        // ... error handling
    }
}
```

### Fix 6: Check Multipart Configuration

Ensure multipart is enabled in `application.properties`:

```properties
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

## Debugging Steps

1. **Check if JWT token is being sent:**
   - Open browser DevTools → Network tab
   - Look for the `POST /assignments/submissions` request
   - Verify the `Authorization` header is present: `Bearer <token>`

2. **Check backend logs:**
   - Look for JWT filter execution
   - Check if authentication is being set

3. **Test with Postman:**
   - Create a POST request to `http://localhost:8082/assignments/submissions`
   - Set `Authorization: Bearer <token>` header
   - Use form-data with:
     - `assignmentId`: `13`
     - `content`: `"Test submission"`
     - `files`: (select file)

## Common Issues

1. **JWT Filter Order**: The JWT filter must run **before** multipart processing
2. **Content-Type Header**: Don't manually set `Content-Type` for FormData requests
3. **Endpoint Path**: Ensure the path matches exactly: `/assignments/submissions`
4. **Security Config**: Make sure the endpoint is not blocked by security rules

## Summary

The most likely issue is that the JWT filter is not processing the Authorization header for multipart requests. Ensure:

1. ✅ JWT filter runs before multipart processing
2. ✅ Endpoint path is correct: `/assignments/submissions`
3. ✅ Security configuration allows the endpoint
4. ✅ Frontend sends the Authorization header correctly
5. ✅ Backend uses `@RequestParam` or `@RequestPart` correctly

After implementing these fixes, the submission should work correctly.






