# JwtFilter Fix - loadUserByUsername Method Issue

## Problem
`UserService` implements `UserDetailsService`, but the compiler doesn't recognize `loadUserByUsername()` method when calling it on `UserService` type.

## Solution
Inject `UserDetailsService` instead of `UserService` in `JwtFilter`, since you only need the `loadUserByUsername()` method.

---

## Fixed JwtFilter.java

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

    // ✅ FIX: Inject UserDetailsService instead of UserService
    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();

        // ⛔ SKIP JWT filter for these paths
        if (path.startsWith("/auth/login") ||
            path.startsWith("/auth/register") ||
            path.startsWith("/auth/refresh") ||
            request.getMethod().equals("OPTIONS")) 
        {
            filterChain.doFilter(request, response);
            return;
        }

        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);

            try {
                String username = jwtUtil.extractUsername(token);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // ✅ FIX: Now using userDetailsService instead of userService
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (jwtUtil.validateToken(token, userDetails)) {
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    } else {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\":\"invalid_or_expired_token\"}");
                        return;
                    }
                }
            } catch (Exception ex) {
                logger.error("JWT validation error: " + ex.getMessage(), ex);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"invalid_or_expired_token\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
```

---

## Alternative Solution (If you need UserService for other methods)

If you need both `UserService` and `UserDetailsService` in the same class, you can inject both:

```java
package com.elearnhub.teacher_service.util;

import com.elearnhub.teacher_service.service.UserService;
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

    // ✅ Inject both if needed
    @Autowired
    private UserDetailsService userDetailsService;  // For loadUserByUsername
    
    // @Autowired
    // private UserService userService;  // For other UserService methods if needed

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();

        // ⛔ SKIP JWT filter for these paths
        if (path.startsWith("/auth/login") ||
            path.startsWith("/auth/register") ||
            path.startsWith("/auth/refresh") ||
            request.getMethod().equals("OPTIONS")) 
        {
            filterChain.doFilter(request, response);
            return;
        }

        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);

            try {
                String username = jwtUtil.extractUsername(token);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // ✅ Use userDetailsService for loadUserByUsername
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (jwtUtil.validateToken(token, userDetails)) {
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    } else {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\":\"invalid_or_expired_token\"}");
                        return;
                    }
                }
            } catch (Exception ex) {
                logger.error("JWT validation error: " + ex.getMessage(), ex);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"invalid_or_expired_token\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
```

---

## Why This Works

1. **Spring Auto-Discovery**: When you inject `UserDetailsService`, Spring automatically finds beans that implement this interface. Since your `UserService` implements `UserDetailsService`, Spring will inject it.

2. **Type Safety**: By injecting `UserDetailsService`, you're programming to the interface, which makes the `loadUserByUsername()` method available and type-safe.

3. **Clean Separation**: `JwtFilter` only needs `loadUserByUsername()`, so injecting `UserDetailsService` is more appropriate than injecting the entire `UserService`.

---

## Important: Make Sure UserService Implements UserDetailsService

Your `UserService` class must properly implement `UserDetailsService`:

```java
@Service
@Transactional
public class UserService implements UserDetailsService {
    
    // ... other methods ...
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
    }
}
```

---

## Summary

**Change in JwtFilter:**
- ❌ `@Autowired private UserService userService;`
- ✅ `@Autowired private UserDetailsService userDetailsService;`

**Change in method call:**
- ❌ `userService.loadUserByUsername(username)`
- ✅ `userDetailsService.loadUserByUsername(username)`

**Add import:**
```java
import org.springframework.security.core.userdetails.UserDetailsService;
```

This fix will resolve the compilation error and make your code cleaner and more maintainable!

