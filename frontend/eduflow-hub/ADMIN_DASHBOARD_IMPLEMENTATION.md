# Admin Dashboard Implementation

## Overview
Complete admin dashboard with backend API integration for managing users, courses, and viewing system analytics.

## Backend Components

### 1. AdminController.java
**Location**: Root directory (move to proper package)
**Endpoints**:
- `GET /admin/stats` - Dashboard statistics
- `GET /admin/users` - List all users (with role and search filters)
- `GET /admin/users/{userId}` - Get user details
- `PUT /admin/users/{userId}` - Update user
- `DELETE /admin/users/{userId}` - Delete user
- `PUT /admin/users/{userId}/role` - Change user role
- `PUT /admin/users/{userId}/status` - Toggle user status
- `GET /admin/courses` - List all courses
- `POST /admin/courses` - Create course
- `PUT /admin/courses/{courseId}` - Update course
- `DELETE /admin/courses/{courseId}` - Delete course
- `GET /admin/analytics/users` - User analytics
- `GET /admin/analytics/courses` - Course analytics
- `GET /admin/analytics/activity` - Activity analytics
- `GET /admin/reports/top-courses` - Top performing courses
- `GET /admin/reports/recent-activities` - Recent system activities

### 2. DTOs Created
- **AdminStatsDTO.java** - Dashboard statistics
- **UserDTO.java** - User information with additional fields

### 3. Services
- **AdminService.java** - Interface
- **AdminServiceImpl.java** - Implementation with full CRUD operations

## Frontend Components

### 1. Enhanced Admin Dashboard (`src/pages/admin/AdminDashboard.tsx`)
**Features**:
- Real-time statistics from backend
- User distribution breakdown
- Content overview (quizzes, materials, classes)
- Top performing courses
- Responsive grid layout

**API Integration**:
- Fetches `/admin/stats` for dashboard metrics
- Fetches `/admin/reports/top-courses` for course rankings

### 2. Enhanced Users Page (`src/pages/admin/Users.tsx`)
**Features**:
- Real-time user list from database
- Search functionality
- Role-based filtering
- User statistics cards
- Delete user functionality
- Active/inactive status display

**API Integration**:
- `GET /admin/users` - Load users
- `GET /admin/stats` - Load statistics
- `DELETE /admin/users/{id}` - Delete users

### 3. New Courses Page (`src/pages/admin/Courses.tsx`)
**Features**:
- List all courses
- Create new courses
- Edit existing courses
- Delete courses
- Teacher assignment display
- Modal dialog for create/edit

**API Integration**:
- `GET /admin/courses` - Load courses
- `POST /admin/courses` - Create course
- `PUT /admin/courses/{id}` - Update course
- `DELETE /admin/courses/{id}` - Delete course

### 4. System Reports Page (Already exists)
- Static analytics and metrics
- Can be enhanced with real API data

## Security

All admin endpoints are protected with:
```java
@PreAuthorize("hasRole('ADMIN')")
```

CORS configured for:
```java
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
```

## Database Requirements

The admin features use existing tables:
- `users` - User management
- `course` - Course management
- `class_entity` - Class statistics
- `assignments` - Assignment counts
- `quiz` - Quiz counts
- `materials` - Material counts

## Routing

Admin routes added to `src/App.tsx`:
```typescript
/admin - Dashboard
/admin/users - User Management
/admin/courses - Course Management (NEW)
/admin/reports - System Reports
```

## Next Steps

1. **Move Backend Files**: Move all Java files to proper package structure:
   - `AdminController.java` → `com.elearnhub.teacher_service.Controller`
   - `AdminService.java` → `com.elearnhub.teacher_service.service`
   - `AdminServiceImpl.java` → `com.elearnhub.teacher_service.service`
   - `AdminStatsDTO.java` → `com.elearnhub.teacher_service.dto`
   - `UserDTO.java` → `com.elearnhub.teacher_service.dto`

2. **Add Missing Repository Methods**:
   ```java
   // In UserRepository
   Long countByRole(String role);
   List<User> findByRole(String role);
   ```

3. **Test Admin Features**:
   - Login as admin user
   - Navigate to `/admin`
   - Test user management
   - Test course management
   - Verify statistics display

4. **Future Enhancements**:
   - Class management page
   - Assignment overview
   - System settings
   - Audit logs
   - Email notifications
   - Bulk operations
   - Export functionality
   - Advanced analytics charts

## Usage

### Creating an Admin User
Run this SQL to create an admin user:
```sql
INSERT INTO users (username, password, name, email, role) 
VALUES ('admin', '$2a$10$...', 'Admin User', 'admin@elearnhub.com', 'ADMIN');
```

### Accessing Admin Dashboard
1. Login with admin credentials
2. Navigate to `/admin`
3. Use sidebar to access different sections

## API Testing

Test endpoints with curl:
```bash
# Get dashboard stats
curl -X GET http://localhost:8082/admin/stats \
  -H "Authorization: Bearer YOUR_TOKEN"

# Get all users
curl -X GET http://localhost:8082/admin/users \
  -H "Authorization: Bearer YOUR_TOKEN"

# Create course
curl -X POST http://localhost:8082/admin/courses \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"New Course","description":"Course description"}'
```

## Notes

- All admin operations require ADMIN role
- Statistics are calculated in real-time from database
- Some analytics data is mocked and can be enhanced
- User deletion should be handled carefully (consider soft delete)
- Course deletion cascades to related classes
