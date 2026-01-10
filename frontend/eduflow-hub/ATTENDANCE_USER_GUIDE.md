# Attendance System - User Guide 📋

## How It Works

The attendance system allows teachers to:
1. Create attendance sessions for each class
2. Mark students as Present, Absent, or Late
3. View attendance statistics and reports

## For Teachers

### Step 1: Create an Attendance Session

1. **Go to a class:**
   - Navigate to Teacher Dashboard
   - Click on any class

2. **Open Attendance tab:**
   - Click on the "Attendance" tab

3. **Create a session:**
   - Click "Create Session" button
   - Fill in the form:
     - **Title**: e.g., "Monday Class", "Week 1 Session"
     - **Date**: Select the date (usually today)
     - **Time**: Optional - e.g., "10:00 AM"
     - **Description**: Optional notes
   - Click "Create Session"

### Step 2: Mark Attendance

1. **Find the session:**
   - You'll see the session you just created in the list

2. **Click "Mark Attendance":**
   - A dialog opens showing all students in the class

3. **Mark each student:**
   - **Click on a student's name** to toggle their status
   - Status cycles: **Present → Absent → Late → Present**
   - Colors indicate status:
     - 🟢 **Green** = Present
     - 🔴 **Red** = Absent
     - 🟡 **Yellow** = Late

4. **Save:**
   - Click "Save Attendance" when done
   - All attendance is saved to the database

### Step 3: View Statistics

1. **Click "Statistics" tab:**
   - See overall attendance for all students

2. **View details:**
   - Each student shows:
     - Total sessions
     - Present count
     - Absent count
     - Late count
     - **Attendance percentage**

## Visual Guide

### Creating a Session:
```
┌─────────────────────────────────────┐
│ Create Attendance Session           │
├─────────────────────────────────────┤
│ Title: Monday Class                 │
│ Date: 2025-12-03                    │
│ Time: 10:00                         │
│ Description: Regular class          │
│                                     │
│ [Cancel]              [Create]      │
└─────────────────────────────────────┘
```

### Marking Attendance:
```
┌─────────────────────────────────────┐
│ Mark Attendance - Monday Class      │
├─────────────────────────────────────┤
│ Click to toggle: Present → Absent → Late
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ John Doe              [Present] │ │ ← Green
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ Jane Smith            [Absent]  │ │ ← Red
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ Bob Johnson           [Late]    │ │ ← Yellow
│ └─────────────────────────────────┘ │
│                                     │
│ [Cancel]         [Save Attendance]  │
└─────────────────────────────────────┘
```

### Statistics View:
```
┌─────────────────────────────────────┐
│ Class Attendance Statistics         │
├─────────────────────────────────────┤
│ ┌─────────────────────────────────┐ │
│ │ John Doe                        │ │
│ │ 8 present, 1 absent, 1 late     │ │
│ │                          80.0%  │ │
│ │                      10 sessions│ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ Jane Smith                      │ │
│ │ 9 present, 1 absent, 0 late     │ │
│ │                          90.0%  │ │
│ │                      10 sessions│ │
│ └─────────────────────────────────┘ │
└─────────────────────────────────────┘
```

## Quick Workflow

### Daily Attendance (5 steps):

1. **Login** as teacher
2. **Go to class** → Click "Attendance" tab
3. **Create session** → Fill date and title
4. **Mark attendance** → Click each student to toggle status
5. **Save** → Done!

## Status Meanings

### Present (Green) ✅
- Student attended the class
- Counts toward attendance percentage

### Absent (Red) ❌
- Student did not attend
- Does not count toward attendance

### Late (Yellow) ⏰
- Student arrived late
- Counts toward attendance (but marked as late)
- You can decide your policy on late arrivals

## Tips

### For Best Results:

1. **Create sessions regularly:**
   - Create one session per class meeting
   - Use consistent naming (e.g., "Week 1 Day 1")

2. **Mark attendance promptly:**
   - Mark during or right after class
   - Don't wait too long

3. **Use the date field:**
   - Set the correct date for each session
   - This helps with reports and statistics

4. **Check statistics regularly:**
   - Monitor student attendance
   - Identify students with low attendance
   - Take action if needed

## Common Scenarios

### Scenario 1: Regular Class
```
1. Create session: "Monday Class - Week 5"
2. Mark all students who attended as Present
3. Mark absent students as Absent
4. Save
```

### Scenario 2: Student Arrives Late
```
1. Initially mark as Absent
2. When student arrives, click their name twice to change to Late
3. Save
```

### Scenario 3: Correcting Mistakes
```
1. Find the session in the list
2. Click "Mark Attendance" again
3. Change the status by clicking the student
4. Save (it will update the existing record)
```

## Features

### ✅ What You Can Do:

- Create unlimited attendance sessions
- Mark attendance for all students at once
- Change attendance status anytime
- View real-time statistics
- See attendance percentage
- Track attendance over time
- Export data (coming soon)

### 📊 Statistics Include:

- Total sessions attended
- Present count
- Absent count
- Late count
- Attendance percentage
- Per-student breakdown
- Class-wide overview

## Attendance Percentage Calculation

```
Attendance % = (Present Count / Total Sessions) × 100

Example:
- Total Sessions: 10
- Present: 8
- Absent: 2
- Late: 0

Attendance = (8 / 10) × 100 = 80%
```

**Note:** Late arrivals count as present in the percentage calculation.

## For Students (Coming Soon)

Students will be able to:
- View their own attendance
- See attendance percentage
- Check which sessions they missed
- Receive notifications for low attendance

## Keyboard Shortcuts

- **Enter**: Submit form
- **Escape**: Close dialog
- **Click**: Toggle attendance status

## Mobile Support

The attendance system works on mobile devices:
- Responsive design
- Touch-friendly buttons
- Easy to use on tablets
- Works on phones

## Best Practices

### 1. Consistent Naming
```
✅ Good:
- "Week 1 - Monday"
- "Week 1 - Wednesday"
- "Week 2 - Monday"

❌ Bad:
- "Class"
- "Today"
- "Session"
```

### 2. Regular Marking
- Mark attendance every class
- Don't skip sessions
- Keep records up to date

### 3. Fair Policy
- Define your late policy clearly
- Be consistent with all students
- Communicate expectations

### 4. Monitor Trends
- Check statistics weekly
- Identify at-risk students
- Reach out to students with low attendance

## Troubleshooting

### Issue: Can't create session
**Solution:** Make sure you're logged in and have TEACHER role

### Issue: Students not showing
**Solution:** Ensure students are enrolled in the class

### Issue: Can't save attendance
**Solution:** Check internet connection and try again

### Issue: Wrong date
**Solution:** Edit the session or create a new one with correct date

## Data Privacy

- Attendance data is private
- Only teachers can see class attendance
- Students can only see their own (when implemented)
- Data is stored securely in the database

## Future Enhancements

Coming soon:
- 📧 Email reports to parents
- 📊 Export to Excel/PDF
- 📱 Student mobile app
- 🔔 Low attendance alerts
- 📈 Attendance trends graphs
- 🎯 Attendance goals
- 🏆 Perfect attendance badges

---

## Summary

**The attendance system is simple:**
1. Create a session for each class
2. Click students to mark Present/Absent/Late
3. Save and view statistics

**That's it!** Easy attendance tracking for your classes! 📚✅
