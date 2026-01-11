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