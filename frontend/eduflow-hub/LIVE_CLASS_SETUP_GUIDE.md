# Live Classes Feature - Setup & Testing Guide

## ✅ What's Already Done

### Backend (100% Complete):
- ✅ LiveClass entity, DTO, Repository
- ✅ LiveClassService & Implementation
- ✅ LiveClassController with all endpoints
- ✅ SQL schema ready

### Frontend (100% Complete):
- ✅ JitsiMeeting component
- ✅ LiveClassManager component (teacher)
- ✅ LiveClassRoom page
- ✅ Routes added to App.tsx
- ✅ Integration in teacher ClassDetail
- ✅ Integration in student ClassDetail (just added!)
- ✅ All API functions in api.ts

## 🚀 Setup Steps

### Step 1: Run Database Migration

Execute this SQL in your MySQL database:

```sql
-- Run the contents of CREATE_LIVE_CLASSES_TABLE.sql
```

Or run this command:
```bash
mysql -u your_username -p your_database < CREATE_LIVE_CLASSES_TABLE.sql
```

### Step 2: Restart Backend

Restart your Spring Boot application to ensure all new entities and controllers are loaded.

### Step 3: Fix User Role (if needed)

If you're still getting 403 errors, run:
```sql
UPDATE users SET role = 'TEACHER' WHERE username = 'Bhagyashri';
```

Then log out and log back in.

## 🧪 Testing the Feature

### As Teacher:

1. **Navigate to a class:**
   - Go to Teacher Dashboard
   - Click on any class

2. **Schedule a live class:**
   - Scroll to "Live Classes" section
   - Click "Schedule Live Class"
   - Fill in:
     - Title: "Introduction to React"
     - Description: "First live session"
     - Start time: (select a time)
     - End time: (select a time)
     - Max participants: 50
     - Enable recording, chat, screen share
   - Click "Schedule"

3. **Start the live class:**
   - Click "Start Class" button
   - Jitsi meeting opens in full screen
   - You're now the host/moderator

4. **Test Jitsi features:**
   - ✅ Video on/off
   - ✅ Audio mute/unmute
   - ✅ Screen sharing
   - ✅ Chat
   - ✅ Participant list
   - ✅ Settings

5. **End the class:**
   - Click "End Class" button
   - Meeting closes
   - Status changes to "ENDED"

### As Student:

1. **Navigate to enrolled class:**
   - Go to Student Dashboard
   - Click on an enrolled class

2. **View live classes:**
   - Scroll to "Live Classes" section
   - See scheduled/live classes

3. **Join a live class:**
   - When status is "LIVE", click "Join Now"
   - Jitsi meeting opens
   - You can participate

4. **Test participation:**
   - ✅ Turn on video/audio
   - ✅ Use chat
   - ✅ View shared screen
   - ✅ Raise hand

## 🎯 Features to Test

### Teacher Features:
- [ ] Schedule live class
- [ ] Edit scheduled class
- [ ] Cancel scheduled class
- [ ] Start live class
- [ ] End live class
- [ ] View all classes (scheduled/live/ended)
- [ ] Moderator controls in Jitsi

### Student Features:
- [ ] View scheduled classes
- [ ] Join live classes
- [ ] Participate in video/audio
- [ ] Use chat
- [ ] View shared screen
- [ ] Receive notifications (if implemented)

### Jitsi Features:
- [ ] HD video quality
- [ ] Audio quality
- [ ] Screen sharing
- [ ] Chat functionality
- [ ] Participant list
- [ ] Mute/unmute
- [ ] Camera on/off
- [ ] Virtual backgrounds
- [ ] Full-screen mode
- [ ] Mobile support

## 📊 Expected Behavior

### Status Flow:
1. **SCHEDULED** → Class is scheduled for future
2. **LIVE** → Teacher started the class
3. **ENDED** → Teacher ended the class
4. **CANCELLED** → Teacher cancelled the class

### Buttons:
- **SCHEDULED**: Shows "Start Class" (teacher) or "Scheduled" (student)
- **LIVE**: Shows "End Class" (teacher) or "Join Now" (student)
- **ENDED**: Shows "Ended" (disabled)
- **CANCELLED**: Shows "Cancelled" (disabled)

## 🐛 Troubleshooting

### Issue: 403 Forbidden Error
**Solution:** 
```sql
UPDATE users SET role = 'TEACHER' WHERE username = 'Bhagyashri';
```
Then log out and log back in.

### Issue: Jitsi not loading
**Solution:** 
- Check browser console for errors
- Ensure internet connection is stable
- Try a different browser (Chrome recommended)

### Issue: No video/audio
**Solution:**
- Grant camera/microphone permissions in browser
- Check if camera/mic are working in other apps
- Try refreshing the page

### Issue: Can't join meeting
**Solution:**
- Verify class status is "LIVE"
- Check if you're enrolled in the class
- Refresh the page

### Issue: Database error
**Solution:**
- Ensure CREATE_LIVE_CLASSES_TABLE.sql was executed
- Check foreign key constraints (class_entity and users tables must exist)
- Verify backend is restarted

## 🎨 UI Preview

### Teacher View:
```
┌─────────────────────────────────────┐
│ Live Classes                        │
│ Schedule and manage live classes    │
├─────────────────────────────────────┤
│ [Schedule Live Class]               │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ Introduction to React    [LIVE] │ │
│ │ First live session              │ │
│ │ 📅 Dec 3, 2025 6:30 PM         │ │
│ │ 👥 Max: 50                      │ │
│ │                    [End Class]  │ │
│ └─────────────────────────────────┘ │
└─────────────────────────────────────┘
```

### Student View:
```
┌─────────────────────────────────────┐
│ Live Classes                        │
│ 1 live class(es) scheduled          │
├─────────────────────────────────────┤
│ ┌─────────────────────────────────┐ │
│ │ Introduction to React    [LIVE] │ │
│ │ First live session              │ │
│ │ 🕐 Dec 3, 2025 6:30 PM         │ │
│ │ 👥 Max: 50                      │ │
│ │                    [Join Now]   │ │
│ └─────────────────────────────────┘ │
└─────────────────────────────────────┘
```

## 🎉 Success Criteria

You'll know it's working when:
- ✅ Teacher can schedule classes
- ✅ Scheduled classes appear in both teacher and student views
- ✅ Teacher can start a class (status changes to LIVE)
- ✅ Student sees "Join Now" button when class is LIVE
- ✅ Clicking "Join Now" opens Jitsi meeting
- ✅ Video/audio works in the meeting
- ✅ Multiple participants can join
- ✅ Teacher can end the class
- ✅ Status changes to ENDED after ending

## 📝 Next Steps

After testing, you can:
1. Add notifications when class starts
2. Add waiting room feature
3. Add attendance tracking
4. Add recording management
5. Add breakout rooms
6. Self-host Jitsi for more control

## 🔒 Security Notes

- Meeting IDs are unique and auto-generated
- Only enrolled students should be able to join (enforce in backend)
- Teachers have moderator privileges
- Consider adding meeting passwords for sensitive classes

## 💡 Tips

1. **Test with 2 browsers:** Open teacher view in one browser and student view in another
2. **Use incognito mode:** To test with different users simultaneously
3. **Check network:** Ensure stable internet for smooth video
4. **Use headphones:** To avoid echo in testing
5. **Grant permissions:** Allow camera/mic access when prompted

## 🎓 Best Practices

### For Teachers:
- Start class 5 minutes early
- Test audio/video before students join
- Mute students by default
- Use screen share for presentations
- Enable chat for questions

### For Students:
- Join with good internet connection
- Use headphones
- Mute when not speaking
- Keep camera on if possible
- Use raise hand feature

---

**Ready to test!** Follow the steps above and enjoy your new live classes feature! 🚀
