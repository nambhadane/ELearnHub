# Live Classes - Testing with Mobile Device 📱

## What I've Added

✅ **"Copy Link" button** - Easily copy the Jitsi meeting link
✅ **Meeting URL display** - See the direct link in the UI
✅ **Works for both teacher and student views**

## How to Test (Laptop + Mobile)

### Step 1: Teacher on Laptop

1. **Login as teacher** on your laptop
   - Username: Bhagyashri (make sure role is TEACHER)

2. **Go to a class** and scroll to "Live Classes" section

3. **Schedule a live class:**
   - Click "Schedule Live Class"
   - Fill in details
   - Click "Schedule"

4. **Start the class:**
   - Click "Start Class" button
   - Status changes to "LIVE"

5. **You'll see the meeting link displayed:**
   ```
   🎥 Class is live!
   
   📋 https://meet.jit.si/[meeting-id]
   
   💡 Testing tip: Copy this link and open it on your mobile phone!
   ```

6. **Copy the link:**
   - Click the "Copy Link" button
   - OR click the small copy icon next to the URL
   - You'll see: "Link Copied!"

7. **Join the meeting on laptop:**
   - Click "Join Class" button
   - Jitsi opens
   - Allow camera/microphone
   - **You're now the moderator!**

### Step 2: Student on Mobile

1. **Send the link to your mobile:**
   - WhatsApp yourself
   - Email yourself
   - Or just type it in mobile browser

2. **Open the link on mobile:**
   - The link looks like: `https://meet.jit.si/[meeting-id]`
   - Opens in mobile browser (Chrome/Safari)

3. **Join the meeting:**
   - Jitsi opens on mobile
   - Allow camera/microphone permissions
   - Enter your name (e.g., "Student Test")
   - Click "Join meeting"

4. **You're in!**
   - You'll see the teacher's video from laptop
   - Teacher will see you on mobile
   - Both can talk, chat, share screen

## Alternative: Use Student Login on Mobile

Instead of just opening the link, you can also:

1. **Open your app on mobile browser:**
   - Go to: `http://[your-laptop-ip]:5173`
   - Example: `http://192.168.1.100:5173`

2. **Login as student:**
   - Create a student account if needed
   - Enroll in the class

3. **Go to the class:**
   - See "Live Classes" section
   - See the LIVE class with "Join Now" button

4. **Click "Join Now":**
   - Opens Jitsi meeting
   - Joins the same room as teacher

## Quick Test Scenario

### 🎬 5-Minute Test:

1. **Laptop (Teacher):**
   ```
   Login → Go to Class → Schedule Live Class → Start Class
   → Copy Link → Join Class
   ```

2. **Mobile (Student):**
   ```
   Open copied link → Allow permissions → Join meeting
   → See teacher's video!
   ```

3. **Test features:**
   - ✅ Video on both devices
   - ✅ Audio working
   - ✅ Chat messages
   - ✅ Screen share (from laptop)
   - ✅ Mute/unmute

4. **End test:**
   ```
   Laptop → Go back to class page → Click "End Class"
   ```

## What You'll See

### On Laptop (Teacher View):
```
┌─────────────────────────────────────────────┐
│ Introduction to React            [LIVE] 🔴 │
├─────────────────────────────────────────────┤
│ [Join Class] [Copy Link] [End Class]       │
│                                             │
│ 🎥 Class is live!                           │
│                                             │
│ 📋 https://meet.jit.si/abc123xyz           │
│    [Copy icon]                              │
│                                             │
│ 💡 Copy this link and open on mobile!      │
└─────────────────────────────────────────────┘
```

### On Mobile (Opening Link):
```
┌─────────────────────────┐
│   Jitsi Meet            │
├─────────────────────────┤
│                         │
│   [Teacher's Video]     │
│                         │
├─────────────────────────┤
│ Your name:              │
│ [Student Test     ]     │
│                         │
│    [Join meeting]       │
│                         │
└─────────────────────────┘
```

### In Meeting (Both Devices):
```
Laptop Screen:
┌─────────────────────────────────┐
│ [Your Video]  [Mobile Student]  │
│                                 │
│ 🎤 📹 🖥️ 💬 👥 ⚙️ 📞           │
└─────────────────────────────────┘

Mobile Screen:
┌─────────────────┐
│ [Teacher Video] │
│                 │
│ [Your Video]    │
│                 │
│ 🎤 📹 💬 📞    │
└─────────────────┘
```

## Tips for Testing

### For Best Results:
1. **Use headphones** on at least one device to avoid echo
2. **Good internet** on both devices
3. **Allow permissions** when browser asks
4. **Use Chrome** on mobile for best compatibility

### Common Issues:

**Issue: Echo/feedback**
- Solution: Use headphones or mute one device

**Issue: Can't hear on mobile**
- Solution: Check phone volume, unmute in Jitsi

**Issue: Link doesn't work**
- Solution: Make sure you copied the full URL

**Issue: Mobile shows "waiting for moderator"**
- Solution: Teacher must join first on laptop!

## Finding Your Laptop IP (for mobile app access)

### Windows:
```cmd
ipconfig
```
Look for "IPv4 Address" (e.g., 192.168.1.100)

### Then on mobile:
```
http://192.168.1.100:5173
```

## Meeting Link Format

The link will look like:
```
https://meet.jit.si/[unique-meeting-id]
```

Example:
```
https://meet.jit.si/elearnhub-1862B17D
```

- **Same link** = Same meeting room
- **Anyone with link** can join
- **First person** becomes moderator (usually)

## Testing Checklist

- [ ] Teacher schedules class on laptop
- [ ] Teacher starts class on laptop
- [ ] Meeting link appears in UI
- [ ] Click "Copy Link" button
- [ ] Link copied to clipboard
- [ ] Teacher joins meeting on laptop
- [ ] Teacher sees themselves on video
- [ ] Send link to mobile (WhatsApp/Email)
- [ ] Open link on mobile browser
- [ ] Allow camera/mic on mobile
- [ ] Join meeting on mobile
- [ ] See teacher's video on mobile
- [ ] Teacher sees student on laptop
- [ ] Test chat between devices
- [ ] Test mute/unmute
- [ ] Teacher ends class on laptop

## Success!

When it works, you'll have:
- ✅ Teacher on laptop with video
- ✅ Student on mobile with video
- ✅ Both can see and hear each other
- ✅ Chat works between devices
- ✅ Real-time video conferencing!

## Next Steps

After successful testing:
1. ✅ Feature is working!
2. ✅ Can be used in production
3. ✅ Students can join from any device
4. ✅ Works on desktop, mobile, tablet

## Pro Tips

### For Teachers:
- Start class 5 minutes early
- Test your setup before students join
- Keep the meeting link handy
- Share link in class WhatsApp group

### For Students:
- Save the meeting link
- Join from quiet location
- Use headphones
- Good internet connection

---

**Ready to test!** Follow the steps above and you'll see live video working between your laptop and mobile! 🎉
