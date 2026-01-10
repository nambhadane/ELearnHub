# Live Classes Feature - Testing Guide (Eclipse Backend)

## 🎯 Quick Testing Steps

### Step 1: Add Backend Files to Eclipse Project

1. **Open Eclipse** with your backend project

2. **Copy the Java files** from this directory to Eclipse:
   - Right-click on `LiveClass.java` → Copy
   - In Eclipse, navigate to: `src/main/java/com/elearnhub/teacher_service/entity/`
   - Right-click → Paste
   
   Repeat for all files:
   - `LiveClass.java` → `entity/` package
   - `LiveClassDTO.java` → `dto/` package (create if doesn't exist)
   - `LiveClassRepository.java` → `repository/` package
   - `LiveClassService.java` → `service/` package
   - `LiveClassServiceImpl.java` → `service/` package
   - `LiveClassController.java` → `Controller/` package

3. **Fix any import errors** in Eclipse (Ctrl+Shift+O to organize imports)

### Step 2: Create Database Tables

1. **Open MySQL Workbench** or your MySQL client

2. **Connect to your database** (the one your backend uses)

3. **Run this SQL:**
   ```sql
   -- Create live_classes table
   CREATE TABLE IF NOT EXISTS live_classes (
       id BIGINT AUTO_INCREMENT PRIMARY KEY,
       class_id BIGINT NOT NULL,
       title VARCHAR(255) NOT NULL,
       description TEXT,
       scheduled_start_time DATETIME NOT NULL,
       scheduled_end_time DATETIME NOT NULL,
       actual_start_time DATETIME,
       actual_end_time DATETIME,
       status VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED',
       meeting_id VARCHAR(255) UNIQUE,
       meeting_password VARCHAR(255),
       host_id BIGINT NOT NULL,
       recording_url VARCHAR(500),
       allow_recording BOOLEAN NOT NULL DEFAULT FALSE,
       allow_chat BOOLEAN NOT NULL DEFAULT TRUE,
       allow_screen_share BOOLEAN NOT NULL DEFAULT TRUE,
       max_participants INT NOT NULL DEFAULT 100,
       created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
       updated_at DATETIME,
       FOREIGN KEY (class_id) REFERENCES class_entity(id) ON DELETE CASCADE,
       FOREIGN KEY (host_id) REFERENCES users(id) ON DELETE CASCADE,
       INDEX idx_class_id (class_id),
       INDEX idx_status (status),
       INDEX idx_meeting_id (meeting_id),
       INDEX idx_scheduled_start (scheduled_start_time)
   );

   -- Create live_class_participants table
   CREATE TABLE IF NOT EXISTS live_class_participants (
       id BIGINT AUTO_INCREMENT PRIMARY KEY,
       live_class_id BIGINT NOT NULL,
       user_id BIGINT NOT NULL,
       joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
       left_at DATETIME,
       duration_minutes INT,
       FOREIGN KEY (live_class_id) REFERENCES live_classes(id) ON DELETE CASCADE,
       FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
       INDEX idx_live_class (live_class_id),
       INDEX idx_user (user_id)
   );
   ```

4. **Verify tables created:**
   ```sql
   SHOW TABLES LIKE 'live_%';
   ```

### Step 3: Fix User Role (Important!)

Run this SQL to make your user a TEACHER:
```sql
UPDATE users SET role = 'TEACHER' WHERE username = 'Bhagyashri';

-- Verify the change
SELECT id, username, name, role FROM users WHERE username = 'Bhagyashri';
```

### Step 4: Restart Backend in Eclipse

1. **Stop the server** in Eclipse (click the red square in Console)
2. **Clean the project:** Project → Clean → Select your project → OK
3. **Refresh the project:** Right-click project → Refresh (F5)
4. **Start the server:** Right-click project → Run As → Spring Boot App

5. **Check console for errors:**
   - Look for "Started [YourApplication] in X seconds"
   - No red error messages
   - Port 8082 should be listening

### Step 5: Test Backend Endpoints (Optional)

Open a browser or Postman and test:

```
GET http://localhost:8082/live-classes/class/1
Authorization: Bearer YOUR_JWT_TOKEN
```

If you get a response (even empty array), backend is working!

### Step 6: Start Frontend

1. **Open terminal** in VS Code (or your frontend directory)

2. **Start the dev server:**
   ```bash
   npm run dev
   ```

3. **Open browser:** http://localhost:5173 (or the port shown)

### Step 7: Test as Teacher

1. **Login** with username: `Bhagyashri` (make sure role is TEACHER now!)

2. **Navigate to a class:**
   - Go to Teacher Dashboard
   - Click on any class in "My Classes"

3. **Scroll to "Live Classes" section** (should be at the bottom)

4. **Schedule a Live Class:**
   - Click "Schedule Live Class" button
   - Fill in the form:
     ```
     Title: Introduction to React
     Description: First live session
     Start Time: [Select today's date and time - 5 minutes from now]
     End Time: [Select 1 hour later]
     Max Participants: 50
     ✓ Allow Recording
     ✓ Allow Chat
     ✓ Allow Screen Share
     ```
   - Click "Schedule" button

5. **You should see the scheduled class** in the list with status "SCHEDULED"

6. **Start the Live Class:**
   - Wait until the scheduled time (or just click "Start Class" anyway)
   - Click "Start Class" button
   - Status changes to "LIVE"
   - Jitsi meeting opens in a new page

7. **Test Jitsi Meeting:**
   - Allow camera/microphone permissions
   - You should see yourself on video
   - Test these features:
     - ✅ Mute/Unmute microphone
     - ✅ Turn camera on/off
     - ✅ Share screen
     - ✅ Open chat
     - ✅ View participants
     - ✅ Settings

8. **End the Class:**
   - Go back to the class detail page
   - Click "End Class" button
   - Status changes to "ENDED"

### Step 8: Test as Student (Optional)

1. **Open a new incognito/private browser window**

2. **Login as a student** (create one if needed):
   ```sql
   -- Create a test student
   INSERT INTO users (username, password, name, email, role) 
   VALUES ('student1', '$2a$10$7mG43wndY/9Mn3SGtCMlpea...', 'Test Student', 'student@test.com', 'STUDENT');
   
   -- Enroll student in a class
   INSERT INTO class_student (class_id, student_id) 
   VALUES (1, [student_id]);
   ```

3. **Navigate to the class:**
   - Go to Student Dashboard
   - Click on enrolled class

4. **View Live Classes section:**
   - Should see scheduled/live classes
   - When status is "LIVE", see "Join Now" button

5. **Join the Live Class:**
   - Click "Join Now"
   - Jitsi meeting opens
   - You can see the teacher
   - You can participate

## 🎬 Quick Demo Flow

### Scenario: Teacher hosts a live class

1. **Teacher:** Schedule class for "now"
2. **Teacher:** Click "Start Class"
3. **Teacher:** Jitsi opens, teacher is host
4. **Student:** Opens class page, sees "Join Now"
5. **Student:** Clicks "Join Now"
6. **Student:** Jitsi opens, joins the meeting
7. **Both:** Can see/hear each other
8. **Teacher:** Shares screen, student sees it
9. **Student:** Uses chat, teacher sees message
10. **Teacher:** Clicks "End Class"
11. **Both:** Meeting ends, status shows "ENDED"

## ✅ Success Checklist

- [ ] Backend files added to Eclipse
- [ ] No compilation errors in Eclipse
- [ ] Database tables created
- [ ] User role changed to TEACHER
- [ ] Backend restarted successfully
- [ ] Frontend running
- [ ] Can login as teacher
- [ ] Can see "Live Classes" section
- [ ] Can schedule a live class
- [ ] Can start a live class
- [ ] Jitsi meeting opens
- [ ] Video/audio works
- [ ] Can end the class
- [ ] Status updates correctly

## 🐛 Troubleshooting

### Issue: "Live Classes" section not showing
**Solution:** 
- Check browser console for errors
- Verify backend is running (check Eclipse console)
- Check if ClassDetail.tsx has LiveClassManager component

### Issue: 403 Forbidden when scheduling
**Solution:**
```sql
UPDATE users SET role = 'TEACHER' WHERE username = 'Bhagyashri';
```
Then logout and login again.

### Issue: Backend compilation errors in Eclipse
**Solution:**
- Right-click project → Maven → Update Project
- Project → Clean
- Check if all dependencies are downloaded

### Issue: Jitsi not loading
**Solution:**
- Check browser console
- Allow camera/microphone permissions
- Try Chrome browser (best compatibility)
- Check internet connection

### Issue: "Start Class" button doesn't work
**Solution:**
- Check browser console for errors
- Verify backend endpoint: `POST /live-classes/{id}/start`
- Check Eclipse console for backend errors

### Issue: Database foreign key error
**Solution:**
- Ensure `class_entity` table exists
- Ensure `users` table exists
- Check if class_id and host_id are valid

## 📊 Expected Console Output

### Eclipse Console (Backend):
```
Started TeacherServiceApplication in 5.234 seconds
Tomcat started on port(s): 8082 (http)
```

### Browser Console (Frontend):
```
[Vite] connected
[HMR] connected
```

### When scheduling a class:
```
POST http://localhost:8082/live-classes 200 OK
```

### When starting a class:
```
POST http://localhost:8082/live-classes/1/start 200 OK
```

## 🎯 What You Should See

### Teacher View:
![Teacher View]
- "Schedule Live Class" button
- List of scheduled/live/ended classes
- "Start Class" button for scheduled classes
- "End Class" button for live classes
- Status badges (SCHEDULED/LIVE/ENDED)

### Student View:
![Student View]
- List of available live classes
- "Join Now" button for live classes
- "Scheduled" badge for upcoming classes
- Time and participant info

### Jitsi Meeting:
![Jitsi Meeting]
- Full-screen video interface
- Your video feed
- Controls at bottom (mute, camera, share, chat, etc.)
- Participant list
- Settings menu

## 🚀 Next Steps After Testing

Once everything works:
1. ✅ Test with multiple participants
2. ✅ Test on mobile devices
3. ✅ Add notifications when class starts
4. ✅ Add attendance tracking
5. ✅ Add recording management
6. ✅ Consider self-hosting Jitsi for more control

## 💡 Pro Tips

1. **Use 2 browsers:** Test teacher and student views simultaneously
2. **Use headphones:** Avoid echo when testing with multiple tabs
3. **Check network:** Ensure stable internet for smooth video
4. **Grant permissions:** Allow camera/mic when browser asks
5. **Test early:** Schedule a class 2-3 minutes from now for quick testing

## 📞 Need Help?

If you encounter issues:
1. Check Eclipse console for backend errors
2. Check browser console for frontend errors
3. Verify database tables exist
4. Ensure user role is TEACHER
5. Try restarting both backend and frontend

---

**Ready to test!** Follow the steps above and you'll have live video classes working in minutes! 🎉
