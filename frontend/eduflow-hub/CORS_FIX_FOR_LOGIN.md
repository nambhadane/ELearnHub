# CORS Fix for Login/Register - Forbidden Error

## Problem
When trying to login or register, you get a **403 Forbidden** error because CORS is rejecting requests from `http://192.168.0.173:8081`.

**Error in logs:**
```
Reject: 'http://192.168.0.173:8081' origin is not allowed
```

## Root Cause
The `SecurityConfig.java` file only allows `http://localhost:8081` as an allowed origin, but your frontend is running on `http://192.168.0.173:8081` (local network IP address).

## Solution

Update the `corsConfigurationSource()` method in your `SecurityConfig.java` file to include both `localhost` and the IP address.

### Location
The file should be at:
```
teacher-service/src/main/java/com/elearnhub/teacher_service/config/SecurityConfig.java
```

### Fix

**Find this method:**
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:8081")); // ❌ Only localhost
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

**Replace with:**
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    // ✅ Allow both localhost and local network IP
    configuration.setAllowedOrigins(Arrays.asList(
        "http://localhost:8081",
        "http://192.168.0.173:8081"
    ));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

## Alternative Solutions

### Option 1: Allow All Local Network IPs (Development Only)
If your IP changes frequently, you can allow all local network IPs (⚠️ **NOT for production**):

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    // ⚠️ Development only - allows any origin on port 8081
    configuration.setAllowedOriginPatterns(Arrays.asList(
        "http://localhost:*",
        "http://192.168.*.*:*",
        "http://127.0.0.1:*"
    ));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

**Note:** Use `setAllowedOriginPatterns()` instead of `setAllowedOrigins()` when using wildcards.

### Option 2: Use Environment Variable (Recommended for Production)
Make it configurable via `application.properties`:

**In `application.properties`:**
```properties
cors.allowed.origins=http://localhost:8081,http://192.168.0.173:8081
```

**In `SecurityConfig.java`:**
```java
@Value("${cors.allowed.origins:http://localhost:8081}")
private String allowedOrigins;

@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(
        Arrays.asList(allowedOrigins.split(","))
    );
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

## Complete SecurityConfig.java (Updated)

Here's the complete updated `SecurityConfig.java` with the fix:

```java
package com.elearnhub.teacher_service.config;

import com.elearnhub.teacher_service.service.UserService;
import com.elearnhub.teacher_service.util.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService((UserDetailsService) userService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // ✅ FIXED: CORS Configuration with both localhost and IP address
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:8081",
            "http://192.168.0.173:8081"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/login", "/auth/register").permitAll()
                .requestMatchers("/users", "/courses").hasRole("TEACHER")
                .requestMatchers("/lessons/**").hasRole("TEACHER")
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/error").permitAll()
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .exceptionHandling(ex -> ex.authenticationEntryPoint((req, res, e) -> res.sendError(HttpServletResponse.SC_FORBIDDEN)))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

## Steps to Apply Fix

1. **Locate the file:**
   - Navigate to: `teacher-service/src/main/java/com/elearnhub/teacher_service/config/SecurityConfig.java`

2. **Update the `corsConfigurationSource()` method:**
   - Add `"http://192.168.0.173:8081"` to the `setAllowedOrigins()` list

3. **Restart the Spring Boot application:**
   - The application should automatically reload if you have Spring DevTools enabled
   - Otherwise, stop and restart the application

4. **Test:**
   - Try logging in or registering again
   - The CORS error should be resolved

## Verification

After applying the fix, you should see in the logs:
- ✅ No more "Reject: 'http://192.168.0.173:8081' origin is not allowed" messages
- ✅ Successful login/register requests

## Notes

- If your IP address changes (e.g., `192.168.0.174`), you'll need to update it again
- For production, use environment variables or a whitelist of allowed origins
- Never use `setAllowedOrigins(Arrays.asList("*"))` with `setAllowCredentials(true)` - this is a security risk






