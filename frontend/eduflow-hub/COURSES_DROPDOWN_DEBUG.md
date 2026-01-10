# Debug: Courses Dropdown Showing Only One Course

## Issue
When creating a class, only one course appears in the dropdown even though multiple courses exist.

## Debugging Steps

### 1. Check Browser Console
Open browser console (F12) and look for:
- `Fetched courses:` - Should show all courses returned from backend
- `Number of courses:` - Should show the count
- `Rendering course:` - Should log each course being rendered

### 2. Check Backend Response
Verify your backend `/courses` endpoint returns ALL courses for the teacher:

```bash
# Test with curl (replace YOUR_TOKEN with actual JWT token)
curl -X GET http://localhost:8082/courses \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json"
```

Should return an array like:
```json
[
  {"id": 1, "name": "Course 1", "description": "...", "teacherId": 11},
  {"id": 2, "name": "Course 2", "description": "...", "teacherId": 11}
]
```

### 3. Check CourseService Implementation
Verify your `CourseService.getCoursesByTeacherId()` method:

```java
public List<Course> getCoursesByTeacherId(Long teacherId) {
    // Should return ALL courses for this teacher
    return courseRepository.findByTeacherId(teacherId);
}
```

### 4. Check Repository Method
Verify your `CourseRepository` has the correct method:

```java
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByTeacherId(Long teacherId);
}
```

### 5. Frontend Check
The frontend code should display all courses. Check:
- Are all courses in the `courses` state array?
- Is the Select component rendering all items?
- Check browser console for any errors

## Possible Causes

1. **Backend only returning one course** - Check database and repository query
2. **Frontend filtering** - Check if any filtering is happening
3. **Select component issue** - Radix UI Select might have rendering issues with many items

## Quick Fix Test

Try clicking the "Refresh" button next to the course dropdown - this will re-fetch courses and might reveal if it's a caching issue.

## Next Steps

1. Check browser console logs when opening Create Class page
2. Verify backend returns all courses
3. If backend is correct, check Select component rendering
4. Share console logs if issue persists

