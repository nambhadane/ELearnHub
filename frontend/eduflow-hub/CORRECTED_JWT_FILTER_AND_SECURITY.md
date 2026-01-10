# 🔧 Corrected JWT Filter and Security Config

## Problem

The JWT filter is not properly handling errors, so when token validation fails, the request continues as anonymous, resulting in 403 Forbidden.

## Solution

### Fix 1: Update JwtFilter with Proper Error Handling and Logging

```java
package com.elearnhub.teacher_service.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, 
            HttpServletResponse response, 
            FilterChain chain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // ✅ Skip JWT filter for public endpoints
        if (path.startsWith("/auth/login") ||
            path.startsWith("/auth/register") ||
            path.startsWith("/auth/refresh") ||
            method.equals("OPTIONS")) {
            chain.doFilter(request, response);
            return;
        }

        // ✅ DEBUG: Log for submission requests
        if (path.contains("/submissions") && method.equals("POST")) {
            logger.info("🔍 SUBMISSION REQUEST - Path: {}", path);
            logger.info("🔍 Authorization Header: {}", request.getHeader("Authorization"));
            logger.info("🔍 Content-Type: {}", request.getContentType());
        }

        // ✅ Extract Authorization header
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);

            try {
                String username = jwtUtil.extractUsername(token);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    logger.debug("🔍 Extracted username from token: {}", username);

                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (jwtUtil.validateToken(token, userDetails)) {
                        UsernamePasswordAuthenticationToken authToken = 
                            new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        
                        logger.debug("✅ Authentication set for user: {}", username);
                    } else {
                        logger.warn("❌ Token validation failed for user: {}", username);
                        // Don't return here - let Spring Security handle it
                    }
                } else {
                    if (username == null) {
                        logger.warn("❌ Could not extract username from token");
                    }
                }
            } catch (Exception ex) {
                logger.error("❌ JWT validation error: {}", ex.getMessage(), ex);
                // Don't return here - let Spring Security handle it
                // If we return here, the request will be anonymous
            }
        } else {
            // ✅ DEBUG: Log when no Authorization header is found
            if (path.contains("/submissions")) {
                logger.error("❌ NO AUTHORIZATION HEADER FOUND for submission request!");
                logger.error("❌ Request path: {}", path);
                logger.error("❌ Request method: {}", method);
                
                // Log all headers for debugging
                java.util.Enumeration<String> headerNames = request.getHeaderNames();
                logger.error("=== ALL REQUEST HEADERS ===");
                while (headerNames.hasMoreElements()) {
                    String headerName = headerNames.nextElement();
                    logger.error("Header: {} = {}", headerName, request.getHeader(headerName));
                }
            }
        }

        // ✅ Continue filter chain - don't block the request
        chain.doFilter(request, response);
    }
}
```

### Fix 2: Update SecurityConfig (Already Correct, but Verify)

Your `SecurityConfig` looks correct, but make sure it matches this exactly:

```java
package com.elearnhub.teacher_service.config;

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
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
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

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8081"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*")); // ✅ This includes Authorization header
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
                .requestMatchers("/assignments/submissions").hasRole("STUDENT") // ✅ Explicitly allow
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/error").permitAll()
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .exceptionHandling(ex -> ex.authenticationEntryPoint((req, res, e) -> {
                // ✅ Better error logging
                System.err.println("❌ Authentication failed: " + e.getMessage());
                res.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
            }))
            // ✅ CRITICAL: JWT filter must run BEFORE UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

## Key Changes

1. **Added Logging**: The JWT filter now logs when Authorization header is missing or token validation fails
2. **Better Error Handling**: Exceptions are caught and logged, but the filter chain continues (don't block the request)
3. **Debug Information**: For submission requests, all headers are logged to help debug

## Testing Steps

1. **Update the JWT filter** with the code above
2. **Restart the backend**
3. **Try submitting an assignment**
4. **Check the backend logs** - you should see:
   - `🔍 SUBMISSION REQUEST - Path: ...`
   - `🔍 Authorization Header: Bearer ...`
   - Either `✅ Authentication set for user: ...` or `❌ NO AUTHORIZATION HEADER FOUND`

## What to Look For in Logs

### If you see "NO AUTHORIZATION HEADER FOUND":
- The frontend is not sending the Authorization header
- Check browser DevTools → Network tab → Request Headers
- Verify the token is in localStorage

### If you see "Token validation failed":
- The token is expired or invalid
- Check token expiration time
- Try logging out and logging in again

### If you see "Authentication set" but still get 403:
- The `@PreAuthorize` check might be failing
- Check the user's role in the database
- Verify the role is "STUDENT" (uppercase)

## Quick Frontend Check

Verify the frontend is sending the token correctly. In browser DevTools → Network tab:

1. Find the `POST /assignments/submissions` request
2. Check **Request Headers** section
3. Verify `Authorization: Bearer <token>` is present

If it's missing, the issue is in the frontend `api.ts` file.






