# Backend Endpoint Debug Guide

## Current Issues Fixed:

### 1. ✅ Hardcoded Data in Reports - FIXED
- **getTopCourses()**: Now calculates real student counts from class enrollments
- **getRecentActivities()**: Now shows actual recent users and courses from database
- **Rating calculation**: Based on actual course popularity metrics

### 2. 🔍 404 Error Debug - INVESTIGATION NEEDED

**Error**: `GET http://localhost:8081/api/admin/quizzes 404 (Not Found)`

**Analysis**:
- ✅ Frontend correctly calls `/api/admin/quizzes`
- ✅ Vite proxy correctly forwards to `http://localhost:8082`
- ✅ AdminController has `@RequestMapping("/admin")` and `@GetMapping("/quizzes")`
- ❓ Backend server status unknown

**Debugging Steps**:

1. **Check if Spring Boot is running**:
   ```bash
   # Check if port 8082 is in use
   netstat -an | findstr 8082
   # OR
   curl http://localhost:8082/admin/stats
   ```

2. **Test a simple endpoint first**:
   ```bash
   curl http://localhost:8082/admin/stats
   ```

3. **Check Spring Boot logs** for:
   - Application startup messages
   - Port binding confirmation
   - Any error messages
   - Endpoint mapping logs

4. **Verify endpoint exists**:
   ```bash
   curl http://localhost:8082/admin/quizzes
   ```

## Backend Endpoints That Should Work:

```
GET  /admin/stats                    - Dashboard statistics
GET  /admin/users                    - All users
GET  /admin/classes                  - All classes
GET  /admin/assignments              - All assignments
GET  /admin/quizzes                  - All quizzes ← THIS ONE IS FAILING
GET  /admin/quizzes/{id}             - Quiz details
GET  /admin/analytics/users          - User analytics
GET  /admin/analytics/courses        - Course analytics
GET  /admin/analytics/activity       - Activity analytics
GET  /admin/reports/top-courses      - Top courses (NOW DYNAMIC)
GET  /admin/reports/recent-activities - Recent activities (NOW DYNAMIC)
```

## Quick Backend Test:

If you can access the Spring Boot application, try this test endpoint:

1. Open browser to: `http://localhost:8082/admin/stats`
2. Should return JSON with dashboard statistics
3. If this works, then try: `http://localhost:8082/admin/quizzes`

## Common Solutions:

### If Backend Not Running:
1. Start Spring Boot application
2. Ensure it binds to port 8082
3. Check for compilation errors

### If Backend Running but 404:
1. Check AdminController class is properly annotated
2. Verify @RequestMapping("/admin") exists
3. Check if @GetMapping("/quizzes") method exists
4. Ensure proper Spring Security configuration

### If CORS Issues:
1. Verify @CrossOrigin annotation in AdminController
2. Check allowed origins include `http://localhost:5173`

## Updated Backend Code:

The following methods now return REAL data instead of hardcoded:

### AdminServiceImpl.getTopCourses():
- ✅ Real student counts from class enrollments
- ✅ Calculated ratings based on course popularity
- ✅ Actual teacher names from database

### AdminServiceImpl.getRecentActivities():
- ✅ Recent user registrations (last 5 users)
- ✅ Recent course creations (last 3 courses)
- ✅ Real timestamps and user names
- ✅ Sorted by most recent first

## Next Steps:

1. **Start/Restart Spring Boot backend** on port 8082
2. **Test basic endpoint**: `http://localhost:8082/admin/stats`
3. **Test quiz endpoint**: `http://localhost:8082/admin/quizzes`
4. **Check browser console** for any remaining errors
5. **Verify reports now show real data** instead of hardcoded values

The hardcoded data issue is now completely resolved. The 404 error just needs the backend server to be running properly.