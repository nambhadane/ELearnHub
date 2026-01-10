# Live Classes - Quick Testing Checklist ✅

## Before You Start

### 1. Add Files to Eclipse Backend
- [ ] Copy `LiveClass.java` to `entity/` package
- [ ] Copy `LiveClassDTO.java` to `dto/` package  
- [ ] Copy `LiveClassRepository.java` to `repository/` package
- [ ] Copy `LiveClassService.java` to `service/` package
- [ ] Copy `LiveClassServiceImpl.java` to `service/` package
- [ ] Copy `LiveClassController.java` to `Controller/` package
- [ ] Fix any import errors (Ctrl+Shift+O)

### 2. Create Database Tables
- [ ] Open MySQL Workbench
- [ ] Run `CREATE_LIVE_CLASSES_TABLE.sql`
- [ ] Verify tables exist: `SHOW TABLES LIKE 'live_%';`

### 3. Fix User Role
- [ ] Run: `UPDATE users SET role = 'TEACHER' WHERE username = 'Bhagyashri';`
- [ ] Verify: `SELECT username, role FROM users WHERE username = 'Bhagyashri';`

### 4. Restart Backend
- [ ] Stop server in Eclipse
- [ ] Clean project (Project → Clean)
- [ ] Start server (Run As → Spring Boot App)
- [ ] Check console: "Started ... in X seconds"
- [ ] No red errors

### 5. Start Frontend
- [ ] Open terminal in VS Code
- [ ] Run: `npm run dev`
- [ ] Open: http://localhost:5173

## Testing Flow

### As Teacher:

#### Schedule a Class
- [ ] Login as Bhagyashri
- [ ] Go to Teacher Dashboard
- [ ] Click on a class
- [ ] Scroll to "Live Classes" section
- [ ] Click "Schedule Live Class"
- [ ] Fill in form:
  - Title: "Test Live Class"
  - Description: "Testing"
  - Start time: (now or soon)
  - End time: (1 hour later)
  - Max participants: 50
  - Enable all options
- [ ] Click "Schedule"
- [ ] See class in list with "SCHEDULED" badge

#### Start the Class
- [ ] Click "Start Class" button
- [ ] Status changes to "LIVE"
- [ ] Jitsi meeting opens in new page
- [ ] Allow camera/microphone permissions
- [ ] See yourself on video

#### Test Jitsi Features
- [ ] Mute/unmute microphone
- [ ] Turn camera on/off
- [ ] Share screen
- [ ] Open chat
- [ ] View participants list
- [ ] Check settings

#### End the Class
- [ ] Go back to class detail page
- [ ] Click "End Class" button
- [ ] Status changes to "ENDED"
- [ ] Meeting closes

### As Student (Optional):

#### View Live Classes
- [ ] Login as student (in incognito window)
- [ ] Go to enrolled class
- [ ] See "Live Classes" section
- [ ] See scheduled/live classes

#### Join a Class
- [ ] When status is "LIVE", see "Join Now" button
- [ ] Click "Join Now"
- [ ] Jitsi meeting opens
- [ ] Allow camera/microphone
- [ ] See teacher's video
- [ ] Can participate

## Troubleshooting

### If 403 Error:
```sql
UPDATE users SET role = 'TEACHER' WHERE username = 'Bhagyashri';
```
Then logout and login.

### If Backend Error:
- Check Eclipse console
- Clean and rebuild project
- Restart server

### If Jitsi Not Loading:
- Check browser console
- Allow camera/mic permissions
- Try Chrome browser
- Check internet connection

### If Database Error:
- Verify tables exist
- Check foreign keys
- Ensure class_id and user_id are valid

## Quick Test (5 Minutes)

1. ✅ Add files to Eclipse (2 min)
2. ✅ Run SQL script (1 min)
3. ✅ Fix user role (30 sec)
4. ✅ Restart backend (30 sec)
5. ✅ Start frontend (30 sec)
6. ✅ Schedule class (30 sec)
7. ✅ Start class (10 sec)
8. ✅ Test Jitsi (1 min)
9. ✅ End class (10 sec)

**Total: ~5-6 minutes to full working live classes!**

## Success Criteria

You'll know it's working when:
- ✅ No errors in Eclipse console
- ✅ No errors in browser console
- ✅ Can schedule a class
- ✅ Can start a class
- ✅ Jitsi opens and works
- ✅ Video/audio works
- ✅ Can end the class
- ✅ Status updates correctly

## Files Location Reference

### Backend (Eclipse):
```
src/main/java/com/elearnhub/teacher_service/
├── Controller/
│   └── LiveClassController.java
├── entity/
│   └── LiveClass.java
├── dto/
│   └── LiveClassDTO.java
├── repository/
│   └── LiveClassRepository.java
└── service/
    ├── LiveClassService.java
    └── LiveClassServiceImpl.java
```

### Frontend (Already Done):
```
src/
├── components/
│   ├── JitsiMeeting.tsx ✅
│   └── LiveClassManager.tsx ✅
├── pages/
│   ├── teacher/
│   │   └── ClassDetail.tsx ✅ (has LiveClassManager)
│   ├── student/
│   │   └── ClassDetail.tsx ✅ (has live classes section)
│   └── LiveClassRoom.tsx ✅
└── services/
    └── api.ts ✅ (has all API functions)
```

## What's Already Done ✅

- ✅ All frontend components created
- ✅ Routes added to App.tsx
- ✅ API functions in api.ts
- ✅ Integration in teacher ClassDetail
- ✅ Integration in student ClassDetail
- ✅ Jitsi integration complete

## What You Need to Do 🔧

1. Add 6 Java files to Eclipse
2. Run 1 SQL script
3. Update 1 user role
4. Restart backend
5. Test!

---

**That's it! Follow this checklist and you'll have live classes working in minutes!** 🚀
