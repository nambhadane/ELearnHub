# Student Attendance View - User Guide

## 🎓 Overview

Students can now view their attendance records and statistics directly from their class detail page.

## ✨ Features

### 1. **Attendance Statistics Dashboard**
- **Overall Attendance Percentage**: Large visual display with progress bar
- **Total Sessions**: Number of attendance sessions recorded
- **Present Count**: Number of times marked present (green)
- **Absent Count**: Number of times marked absent (red)
- **Late Count**: Number of times marked late (yellow)

### 2. **Detailed Attendance Records**
Each attendance record shows:
- Session title and description
- Date and time of the session
- Your attendance status (Present/Absent/Late) with color-coded badges
- When attendance was marked
- Any notes added by the teacher

### 3. **Excel Export**
- Click "Export Excel" to download your attendance report
- Includes two sheets:
  - **My Attendance**: All your attendance records
  - **My Statistics**: Your overall statistics
- Filename: `My_Attendance_2025-12-03.xlsx`

## 📍 How to Access

1. **Login as Student**
2. **Go to "My Classes"**
3. **Click on any class**
4. **Scroll down** to the "My Attendance" section

## 🎨 Visual Indicators

### Status Badges
- 🟢 **Green Badge (Present)**: You attended the session
- 🔴 **Red Badge (Absent)**: You were absent
- 🟡 **Yellow Badge (Late)**: You arrived late
- ⚪ **Gray Badge (Not Marked)**: Attendance not yet recorded

### Statistics Cards
- **Total Sessions**: Gray background
- **Present**: Green background
- **Absent**: Red background
- **Late**: Yellow background

## 📊 Excel Report Structure

### Sheet 1: My Attendance
| Session | Date | Time | Status | Marked At | Notes |
|---------|------|------|--------|-----------|-------|
| Monday Class | 12/3/2025 | 10:00 | Present | 12/3/2025 10:05 AM | |

### Sheet 2: My Statistics
| Metric | Value |
|--------|-------|
| Total Sessions | 10 |
| Present | 9 |
| Absent | 1 |
| Late | 0 |
| Attendance Percentage | 90.0% |

## 🔍 What Students See

### If No Attendance Records
- Empty state with calendar icon
- Message: "No attendance records found"
- Explanation: "Your attendance will appear here once marked by your teacher"

### With Attendance Records
- List of all sessions where attendance was marked
- Each record shows:
  - Session details
  - Your status
  - Date and time
  - Teacher's notes (if any)

## 💡 Tips for Students

1. **Check Regularly**: Review your attendance after each class
2. **Monitor Percentage**: Keep track of your attendance percentage
3. **Export Reports**: Download Excel reports for your records
4. **Contact Teacher**: If you see any discrepancies, contact your teacher
5. **Stay Informed**: Check notes from teachers for important information

## 🎯 Benefits

- ✅ **Transparency**: See exactly when you were marked present/absent
- ✅ **Self-Monitoring**: Track your own attendance performance
- ✅ **Documentation**: Export reports for personal records
- ✅ **Accountability**: Stay aware of your attendance status
- ✅ **Easy Access**: View anytime from your class page

## 📱 Mobile Friendly

The attendance view is fully responsive and works great on:
- Desktop computers
- Tablets
- Mobile phones

## 🔐 Privacy

- Students can only see their own attendance records
- No access to other students' attendance
- All data is securely fetched from the backend

## 🚀 Technical Details

### Components
- **StudentAttendanceView.tsx**: Main component for student attendance
- Integrated into **ClassDetail.tsx** (student version)

### API Endpoints Used
- `GET /attendance/sessions/class/{classId}`: Get all sessions
- `GET /attendance/statistics/student`: Get student statistics

### Features
- Real-time data loading
- Error handling with toast notifications
- Excel export with xlsx library
- Responsive design with Tailwind CSS
- Accessible UI components

## 📝 Notes

- Attendance records appear only after teacher marks them
- Statistics update automatically when new attendance is marked
- Export button is disabled if no records exist
- All dates and times are displayed in local timezone

---

**Need Help?** Contact your teacher or system administrator if you have questions about your attendance records.
