# 🔧 Features That Need Fixing

## Analysis of Current State

Based on code review, here are the features that need attention:

### ✅ Already Working:
1. Authentication (Login/Logout)
2. Dashboard (Student & Teacher) - Real-time data
3. Classes (View, Create, Enroll)
4. Assignments (Create, View, Submit)
5. Messaging System (Send/Receive messages with files)
6. Notifications (Bell icon with real-time updates)
7. Profile Pages (View profile information)
8. Settings Pages (UI complete, needs backend)
9. Submissions (View, Grade)
10. Dark Mode Toggle

### ⚠️ Needs Backend Integration:
1. **Settings - Password Change** (Frontend ready, needs API)
2. **Settings - Save Preferences** (Frontend ready, needs API)
3. **Profile - Edit Information** (Frontend ready, needs API)
4. **Class Materials** (TODO comment found)

### 🔍 Let me check each major feature:

## Priority 1: Critical Features

### 1. Class Materials (TODO found)
- Location: `src/pages/teacher/ClassMaterials.tsx`
- Status: Has TODO comment
- Action: Needs backend endpoint

### 2. Student Class Schedule
- Location: `src/pages/student/ClassDetail.tsx`
- Status: Shows "Coming soon"
- Action: Needs implementation

## Priority 2: Backend Integration Needed

### 3. Settings - Password Change
- Frontend: ✅ Complete
- Backend: ❌ Needs API endpoint
- Action: Create password change endpoint

### 4. Settings - Save Preferences
- Frontend: ✅ Complete
- Backend: ❌ Needs API endpoint
- Action: Create preferences save endpoint

### 5. Profile - Edit Information
- Frontend: ✅ Complete
- Backend: ❌ Needs API endpoint
- Action: Create profile update endpoint

## What would you like me to fix first?

Please tell me which features you want me to work on:

1. **Class Materials** - Add ability to upload/view course materials
2. **Class Schedule** - Show class schedule/timetable
3. **Password Change Backend** - Make password change actually work
4. **Profile Edit Backend** - Make profile editing save to database
5. **Settings Backend** - Make settings persist to database
6. **Something else** - Tell me what's not working

Or I can fix them all systematically! Just let me know your priority.
