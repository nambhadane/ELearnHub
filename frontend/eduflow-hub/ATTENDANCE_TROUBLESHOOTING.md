# Attendance System - Troubleshooting 400 Error

## Issue
Getting `400 Bad Request` when trying to create attendance session.

## Checklist

### 1. ✅ Database Tables Created?
Run this SQL in MySQL:
```sql
-- Check if tables exist
SHOW TABLES LIKE 'attendance%';

-- If not, run CREATE_ATTENDANCE_TABLES.sql
```

### 2. ✅ Backend Files Added to Eclipse?
Verify these files are in your Eclipse project:
- `AttendanceSession.java` → `entity/`
- `AttendanceRecord.java` → `entity/`
- `AttendanceSessionDTO.java` → `dto/`
- `AttendanceRecordDTO.java` → `dto/`
- `AttendanceStatisticsDTO.java` → `dto/`
- `AttendanceSessionRepository.java` → `repository/`
- `AttendanceRecordRepository.java` → `repository/`
- `AttendanceService.java` → `service/`
- `AttendanceServiceImpl.java` → `service/`
- `AttendanceController.java` → `Controller/`

### 3. ✅ Backend Restarted?
- Stop the server in Eclipse
- Clean project (Project → Clean)
- Restart the server

### 4. ✅ Check Eclipse Console
Look for errors like:
- `AttendanceController not found`
- `Table 'attendance_session' doesn't exist`
- `Cannot create bean`

### 5. ✅ Test Backend Endpoint Directly
Open Postman or browser and test:
```
GET http://localhost:8082/attendance/sessions/class/1
Authorization: Bearer YOUR_TOKEN
```

### 6. ✅ Check Browser Network Tab
- Open DevTools → Network
- Try creating session
- Click on the failed request
- Check "Payload" tab - what data is being sent?
- Check "Response" tab - what error message?

## Common Issues

### Issue: "Table doesn't exist"
**Solution:** Run `CREATE_ATTENDANCE_TABLES.sql`

### Issue: "AttendanceController not found"
**Solution:** 
- Verify `AttendanceController.java` is in `Controller/` package
- Restart backend

### Issue: "Cannot create bean"
**Solution:**
- Check all files are in correct packages
- Verify no compilation errors
- Clean and rebuild project

### Issue: "401 Unauthorized"
**Solution:**
- Check if you're logged in
- Verify token is being sent
- Check token hasn't expired

### Issue: "403 Forbidden"
**Solution:**
- Verify user role is TEACHER
- Check `@PreAuthorize` annotations

## Quick Fix

If still not working, try this:

1. **Check backend is running:**
   ```
   http://localhost:8082/actuator/health
   ```
   Should return: `{"status":"UP"}`

2. **Check if endpoint exists:**
   Look in Eclipse console for:
   ```
   Mapped "{[/attendance/sessions],methods=[POST]}"
   ```

3. **Verify data being sent:**
   Open browser console and add this before the API call:
   ```javascript
   console.log('Sending:', session);
   ```

4. **Check backend logs:**
   Eclipse console should show the incoming request and any errors.

## Expected Request Format

The frontend should send:
```json
{
  "classId": 1,
  "sessionDate": "2025-12-03",
  "sessionTime": "14:30",
  "title": "Monday Class",
  "description": "Regular session",
  "createdBy": 1
}
```

## Expected Response

Success (200):
```json
{
  "id": 1,
  "classId": 1,
  "sessionDate": "2025-12-03",
  "sessionTime": "14:30",
  "title": "Monday Class",
  "description": "Regular session",
  "createdBy": 1,
  "createdByName": "Teacher Name",
  "createdAt": "2025-12-03T14:30:00",
  "totalStudents": 0,
  "presentCount": 0,
  "absentCount": 0,
  "lateCount": 0,
  "attendancePercentage": 0.0
}
```

Error (400):
```json
{
  "message": "Error description here"
}
```

## Debug Steps

1. **Add console.log in AttendanceManager.tsx:**
   ```typescript
   const handleCreateSession = async (e: React.FormEvent<HTMLFormElement>) => {
     e.preventDefault();
     const formData = new FormData(e.currentTarget);
     
     const session: AttendanceSessionDTO = {
       classId,
       sessionDate: formData.get("sessionDate") as string,
       sessionTime: formData.get("sessionTime") as string,
       title: formData.get("title") as string,
       description: formData.get("description") as string,
       createdBy: user.id,
     };
     
     console.log('Creating session:', session); // ADD THIS
     console.log('User:', user); // ADD THIS
     
     try {
       await createAttendanceSession(session);
       // ...
     }
   }
   ```

2. **Check what's being sent:**
   - Open browser DevTools
   - Go to Network tab
   - Create a session
   - Click on the request
   - Check "Payload" tab

3. **Check backend receives it:**
   - Look at Eclipse console
   - Should see: `POST /attendance/sessions`
   - Should see any error messages

## Still Not Working?

Share these details:
1. Eclipse console error message
2. Browser network tab response
3. Data being sent (from console.log)
4. MySQL table structure: `DESCRIBE attendance_session;`

---

**Most Common Fix:** Make sure you've run the SQL script and restarted the backend!
