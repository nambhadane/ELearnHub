# 🔴 CRITICAL Fix for Assignment Submission 403 Error

## Problem

The JWT token is **NOT being recognized** for multipart requests. The logs show:
```
Set SecurityContextHolder to anonymous SecurityContext
```

This means the JWT filter is not processing the Authorization header before Spring Security rejects the request.

## Root Cause

For multipart requests, the JWT filter must run **BEFORE** the multipart resolver processes the request. If the multipart resolver runs first, it may consume the request body, making the Authorization header inaccessible.

## Solution

### Fix 1: Ensure JWT Filter Runs Before Multipart Processing

**Critical**: The JWT filter must be added **BEFORE** any multipart processing. Update your `SecurityConfig`:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Autowired
    private JwtFilter jwtFilter;
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/assignments/submissions").hasRole("STUDENT") // ✅ Explicitly allow
                .anyRequest().authenticated()
            )
            // ✅ CRITICAL: Add JWT filter BEFORE UsernamePasswordAuthenticationFilter
            // This ensures it runs BEFORE multipart processing
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
            
        return http.build();
    }
}
```

### Fix 2: Update JWT Filter to Handle Multipart Requests

Make sure your `JwtFilter` extracts the Authorization header **BEFORE** any request body processing:

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
        
        // ✅ CRITICAL: Extract Authorization header FIRST, before any body processing
        String authorizationHeader = request.getHeader("Authorization");
        
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            
            try {
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
            } catch (Exception e) {
                // Log error but don't block the request - let Spring Security handle it
                logger.error("JWT validation error: " + e.getMessage(), e);
            }
        }
        
        // ✅ Continue filter chain - this allows multipart processing AFTER authentication is set
        chain.doFilter(request, response);
    }
}
```

### Fix 3: Verify AssignmentController Endpoint

Make sure your `AssignmentController` has the correct endpoint mapping:

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
            Authentication authentication) {  // ✅ Authentication should be available here
        try {
            // Get authenticated student
            String username = authentication.getName();
            User student = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            // Validate assignmentId
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

            // Handle file uploads
            if (files != null && !files.isEmpty()) {
                List<String> filePaths = new ArrayList<>();
                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        String originalFilename = file.getOriginalFilename();
                        String timestamp = String.valueOf(System.currentTimeMillis());
                        String filename = timestamp + "_" + originalFilename;

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

                if (!filePaths.isEmpty()) {
                    submissionDTO.setFilePath(String.join(",", filePaths));
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

### Fix 4: Alternative - Use HttpServletRequestWrapper for Multipart

If the above doesn't work, you may need to ensure the Authorization header is accessible. Add this to your `JwtFilter`:

```java
@Override
protected void doFilterInternal(
        HttpServletRequest request, 
        HttpServletResponse response, 
        FilterChain chain) throws ServletException, IOException {
    
    // ✅ For multipart requests, ensure we can read the header
    String authorizationHeader = request.getHeader("Authorization");
    
    // ✅ Also try to get it from the request attributes (in case multipart already processed)
    if (authorizationHeader == null) {
        authorizationHeader = (String) request.getAttribute("Authorization");
    }
    
    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
        String token = authorizationHeader.substring(7);
        
        try {
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
        } catch (Exception e) {
            logger.error("JWT validation error: " + e.getMessage(), e);
        }
    }
    
    chain.doFilter(request, response);
}
```

### Fix 5: Check Filter Order in SecurityConfig

**Most Important**: Ensure the JWT filter is added **BEFORE** `UsernamePasswordAuthenticationFilter`:

```java
.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
```

**NOT**:
```java
.addFilterAfter(jwtFilter, ...);  // ❌ WRONG
```

### Fix 6: Debug - Add Logging to JWT Filter

Add logging to see if the Authorization header is being received:

```java
@Override
protected void doFilterInternal(
        HttpServletRequest request, 
        HttpServletResponse response, 
        FilterChain chain) throws ServletException, IOException {
    
    String path = request.getRequestURI();
    String method = request.getMethod();
    
    // ✅ DEBUG: Log all headers for multipart requests
    if (path.contains("/submissions") && method.equals("POST")) {
        logger.info("=== SUBMISSION REQUEST DEBUG ===");
        logger.info("Path: " + path);
        logger.info("Content-Type: " + request.getContentType());
        logger.info("Authorization header: " + request.getHeader("Authorization"));
        
        // Log all headers
        java.util.Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            logger.info("Header: " + headerName + " = " + request.getHeader(headerName));
        }
    }
    
    String authorizationHeader = request.getHeader("Authorization");
    
    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
        // ... rest of JWT processing
    } else {
        if (path.contains("/submissions")) {
            logger.error("❌ NO AUTHORIZATION HEADER FOUND for submission request!");
        }
    }
    
    chain.doFilter(request, response);
}
```

## Most Likely Issue

The **filter order** is wrong. The JWT filter must run **BEFORE** multipart processing. Check:

1. ✅ `SecurityConfig` uses `.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)`
2. ✅ `JwtFilter` extends `OncePerRequestFilter` (not `Filter`)
3. ✅ Authorization header is extracted **before** `chain.doFilter()`

## Quick Test

Add this temporary logging to see what's happening:

```java
// In JwtFilter.doFilterInternal(), at the very beginning:
logger.info("🔍 JWT Filter - Request: " + request.getMethod() + " " + request.getRequestURI());
logger.info("🔍 Authorization Header: " + request.getHeader("Authorization"));
```

If you see the Authorization header in the logs but authentication is still not set, the issue is in token validation. If you don't see the header, the frontend isn't sending it correctly.

## Summary

1. ✅ Ensure JWT filter runs **BEFORE** `UsernamePasswordAuthenticationFilter`
2. ✅ Extract Authorization header **BEFORE** calling `chain.doFilter()`
3. ✅ Verify endpoint path: `/assignments/submissions`
4. ✅ Verify `@PreAuthorize("hasRole('STUDENT')")` is present
5. ✅ Add explicit security rule: `.requestMatchers("/assignments/submissions").hasRole("STUDENT")`

After these fixes, the submission should work correctly.






