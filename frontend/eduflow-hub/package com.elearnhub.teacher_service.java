package com.elearnhub.teacher_service.util;

// ...existing imports...

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, 
            HttpServletResponse response, 
            FilterChain chain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // ...existing skip logic for /auth/login, /auth/register, /auth/refresh, OPTIONS ...

        // DEBUG for submissions — ensure header read before multipart
-        if (path.contains("/submissions") && method.equals("POST")) {
+        if (path.contains("/submissions") && method.equals("POST")) {
             logger.info("🔍 SUBMISSION REQUEST - Path: {}", path);
-            logger.info("🔍 Authorization Header: {}", request.getHeader("Authorization"));
+            // log both common header names for diagnostics
+            logger.info("🔍 Authorization Header (Authorization): {}", request.getHeader("Authorization"));
+            logger.info("🔍 Authorization Header (authorization): {}", request.getHeader("authorization"));
             logger.info("🔍 Content-Type: {}", request.getContentType());
         }

        // ...existing token extraction and validation logic ...
    }
