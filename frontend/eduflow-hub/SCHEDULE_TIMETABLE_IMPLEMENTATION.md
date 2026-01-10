# Class Schedule/Timetable Feature - Implementation Guide

## ✅ What's Been Created

### Backend Files (Copy to your backend project)

1. **Schedule.java** → `teacher-service/src/main/java/com/elearnhub/teacher_service/entity/Schedule.java`
   - Entity for storing class schedules

2. **ScheduleRepository.java** → `teacher-service/src/main/java/com/elearnhub/teacher_service/repository/ScheduleRepository.java`
   - Database repository

3. **ScheduleDTO.java** → `teacher-service/src/main/java/com/elearnhub/teacher_service/dto/ScheduleDTO.java`
   - Data transfer object

4. **ScheduleService.java** → `teacher-service/src/main/java/com/elearnhub/teacher_service/service/ScheduleService.java`
   - Service interface

5. **ScheduleServiceImpl.java** → `teacher-service/src/main/java/com/elearnhub/teacher_service/service/ScheduleServiceImpl.java`
   - Service implementation

6. **ScheduleController.java** → `teacher-service/src/main/java/com/elearnhub/teacher_service/Controller/ScheduleController.java`
   - REST API endpoints

### Database

7. **CREATE_SCHEDULES_TABLE.sql**
   - Run this SQL to create the schedules table

### Frontend Files

8. **src/services/api.ts** - Schedule API functions added
9. **src/pages/student/Timetable.tsx** - Student timetable view (NEW PAGE)

## 📋 Setup Steps

### 1. Database Setup
```sql
CREATE TABLE IF NOT EXISTS schedules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_id BIGINT NOT NULL,
    day_of_week VARCHAR(20) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    room VARCHAR(100),
    location VARCHAR(255),
    notes VARCHAR(500),
    
    FOREIGN KEY (class_id) REFERENCES class_entity(id) ON DELETE CASCADE,
    
    INDEX idx_class_id (class_id),
    INDEX idx_day_of_week (day_of_week)
);
```

### 2. Copy Backend Files

Copy all Java files to your backend project folders.

### 3. Add Route for Student Timetable

Add to your router (e.g., `src/App.tsx` or router config):
```typescript
{
  path: "/student/timetable",
  element: <Timetable />
}
```

### 4. Add Navigation Link

Add timetable link to student sidebar/navigation.

### 5. Restart Backend

Restart your Spring Boot application.

## 🎯 Features

### For Teachers:
- ✅ Create class schedules (day, time, room, location)
- ✅ Update schedules
- ✅ Delete schedules
- ✅ View schedules for each class
- ✅ Set multiple time slots per class

### For Students:
- ✅ View personal timetable (all enrolled classes)
- ✅ See schedule organized by day of week
- ✅ View class times, rooms, and locations
- ✅ Color-coded classes for easy identification
- ✅ Responsive weekly view

## 🔌 API Endpoints

### Create Schedule (Teacher)
```
POST /schedules
Authorization: Bearer {token}

Body:
{
  "classId": 1,
  "dayOfWeek": "MONDAY",
  "startTime": "09:00:00",
  "endTime": "10:30:00",
  "room": "Room 101",
  "location": "Main Building",
  "notes": "Bring textbook"
}
```

### Get Schedules by Class
```
GET /schedules/class/{classId}
Authorization: Bearer {token}
```

### Get Student Timetable
```
GET /schedules/my-timetable
Authorization: Bearer {token}
```

### Update Schedule (Teacher)
```
PUT /schedules/{id}
Authorization: Bearer {token}

Body: (same as create)
```

### Delete Schedule (Teacher)
```
DELETE /schedules/{id}
Authorization: Bearer {token}
```

## 📅 Days of Week

Valid values for `dayOfWeek`:
- MONDAY
- TUESDAY
- WEDNESDAY
- THURSDAY
- FRIDAY
- SATURDAY
- SUNDAY

## ⏰ Time Format

Times should be in 24-hour format:
- `"09:00:00"` = 9:00 AM
- `"14:30:00"` = 2:30 PM
- `"18:00:00"` = 6:00 PM

## 🎨 Student Timetable Features

- **Weekly View**: Shows all classes organized by day
- **Color Coding**: Each class has a unique color
- **Time Display**: Shows in 12-hour format (AM/PM)
- **Room & Location**: Displays where the class is held
- **Notes**: Shows any additional information
- **Empty State**: Friendly message when no schedule exists

## 🔧 Next Steps - Teacher Schedule Management UI

To add schedule management for teachers, you can:

1. **Add Schedule Tab to Teacher ClassDetail**:
   - Add a "Schedule" tab in the ClassDetail page
   - Show existing schedules for the class
   - Add "Add Schedule" button
   - Show form to create/edit schedules

2. **Quick Implementation**:
   ```typescript
   // In teacher ClassDetail, add a Schedule tab:
   <TabsTrigger value="schedule">Schedule</TabsTrigger>
   
   <TabsContent value="schedule">
     {/* List schedules */}
     {/* Add schedule button */}
     {/* Schedule form dialog */}
   </TabsContent>
   ```

## 📱 Mobile Responsive

The timetable view is fully responsive:
- Desktop: Full weekly view
- Tablet: Stacked day cards
- Mobile: Single column layout

## ✨ Optional Enhancements

- [ ] Drag & drop schedule editing
- [ ] Recurring schedules (every week)
- [ ] Schedule conflicts detection
- [ ] Export to calendar (iCal)
- [ ] Email reminders
- [ ] Schedule change notifications
- [ ] Print timetable
- [ ] Current class highlighting

## 🎉 You're Done!

Students can now view their weekly timetable at `/student/timetable`!

Teachers can create schedules via API (UI can be added to ClassDetail page).
