# Email Verification "Access Denied" Fix

## Problem
Email verification shows "Access Denied" error when clicking the verification link.

## Root Cause
The `/auth/verify-email` endpoint is being blocked by Spring Security or CORS configuration.

## Solution

### 1. Update SecurityConfig in Eclipse
Make sure your SecurityConfig.java in Eclipse has these permitAll() configurations:

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/auth/login", "/auth/register", "/auth/verify-email").permitAll()
    .requestMatchers("/email/verify/**").permitAll()
    // ... other configurations
)
```

### 2. Update CORS Configuration
Make sure your CORS configuration allows port 8081:

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:8081", "http://localhost:3000", "http://localhost:5173"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", source);
    return source;
}
```

### 3. Update Controller CORS Annotations
Update all your controllers to include port 8081:

```java
@CrossOrigin(origins = {"http://localhost:8081", "http://localhost:5173"}, allowCredentials = "true")
```

### 4. Verify Backend Port
Make sure your Spring Boot backend is running on port 8082 (as configured in application.properties):

```properties
server.port=8082
```

### 5. Updated Files to Copy to Eclipse
Copy these updated files to your Eclipse project:
- `application.properties` (updated base URL to port 8081)
- `EmailVerificationController.java` (updated CORS)
- `AuthController.java` (added CORS)
- `AdminController.java` (updated CORS)

## Testing
1. Restart your Spring Boot application in Eclipse
2. Frontend should be running on http://localhost:8081
3. Backend should be running on http://localhost:8082
4. Try the email verification link again

## Expected Result
The email verification should work without "Access Denied" error.