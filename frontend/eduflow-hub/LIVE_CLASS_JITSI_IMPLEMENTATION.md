# Live Class Implementation with Jitsi Meet

## Overview
Implementing live video conferencing using Jitsi Meet - an open-source, production-ready video conferencing solution.

## Why Jitsi Meet?

✅ **Advantages:**
- Open source and free
- No API keys or registration required
- Production-ready and scalable
- Supports 100+ participants
- Built-in features: screen sharing, chat, recording
- Works in all modern browsers
- Can be self-hosted or use public servers
- GDPR compliant

## Backend Implementation (COMPLETED)

### Files Created:
1. ✅ `LiveClass.java` - Entity
2. ✅ `LiveClassDTO.java` - Data Transfer Object
3. ✅ `LiveClassRepository.java` - Database access
4. ✅ `LiveClassService.java` - Service interface
5. ✅ `LiveClassServiceImpl.java` - Service implementation
6. ✅ `LiveClassController.java` - REST API endpoints
7. ✅ `CREATE_LIVE_CLASSES_TABLE.sql` - Database schema

### Database Schema:
```sql
live_classes table:
- id, class_id, title, description
- scheduled_start_time, scheduled_end_time
- actual_start_time, actual_end_time
- status (SCHEDULED, LIVE, ENDED, CANCELLED)
- meeting_id (unique identifier)
- meeting_password
- host_id (teacher)
- recording_url
- allow_recording, allow_chat, allow_screen_share
- max_participants
- created_at, updated_at
```

### API Endpoints:

**Teacher Endpoints:**
- `POST /live-classes` - Schedule live class
- `PUT /live-classes/{id}` - Update live class
- `DELETE /live-classes/{id}` - Cancel live class
- `POST /live-classes/{id}/start` - Start live class
- `POST /live-classes/{id}/end` - End live class
- `GET /live-classes/class/{classId}` - Get class live sessions
- `GET /live-classes/my-classes` - Get teacher's live classes

**Student Endpoints:**
- `GET /live-classes/available/class/{classId}` - Get available live classes
- `POST /live-classes/{id}/join` - Join live class

**Common Endpoints:**
- `GET /live-classes/{id}` - Get live class details
- `GET /live-classes/meeting/{meetingId}` - Get by meeting ID

## Frontend Implementation (TODO)

### Components to Create:

1. **Teacher Components:**
   - `LiveClassScheduler.tsx` - Schedule new live class
   - `LiveClassList.tsx` - View scheduled classes
   - `LiveClassRoom.tsx` - Jitsi meeting room (teacher view)

2. **Student Components:**
   - `AvailableLiveClasses.tsx` - View upcoming/live classes
   - `LiveClassRoom.tsx` - Jitsi meeting room (student view)

3. **Shared Components:**
   - `JitsiMeeting.tsx` - Jitsi iframe wrapper

### Pages to Create:
- `/teacher/live-classes` - Teacher live class management
- `/teacher/live-class/:id` - Join as host
- `/student/live-classes` - Student available classes
- `/student/live-class/:id` - Join as participant

### Integration Steps:

1. **Install Jitsi React SDK:**
```bash
npm install @jitsi/react-sdk
```

2. **Or use Jitsi IFrame API:**
```html
<script src='https://meet.jit.si/external_api.js'></script>
```

3. **Create Jitsi Meeting Component:**
```typescript
const JitsiMeeting = ({ meetingId, userName, userRole }) => {
  // Initialize Jitsi
  // Configure options
  // Handle events
}
```

4. **Configure Jitsi Options:**
```typescript
{
  roomName: meetingId,
  width: '100%',
  height: '100%',
  configOverwrite: {
    startWithAudioMuted: true,
    startWithVideoMuted: false,
    enableWelcomePage: false,
  },
  interfaceConfigOverwrite: {
    TOOLBAR_BUTTONS: [
      'microphone', 'camera', 'desktop', 'chat',
      'raisehand', 'participants-pane', 'tileview'
    ],
  },
  userInfo: {
    displayName: userName,
    email: userEmail,
  }
}
```

## Features Included:

### Core Features:
✅ Schedule live classes
✅ Start/End live classes
✅ Join live classes
✅ Video/Audio streaming
✅ Screen sharing
✅ Chat
✅ Participant list
✅ Raise hand
✅ Mute/Unmute
✅ Camera on/off

### Additional Features (Jitsi Built-in):
✅ Recording (if enabled)
✅ Virtual backgrounds
✅ Noise suppression
✅ Tile view / Speaker view
✅ Full-screen mode
✅ Mobile support
✅ Breakout rooms (Jitsi 8.0+)

## User Workflows:

### Teacher Workflow:
1. Navigate to class detail page
2. Click "Schedule Live Class"
3. Fill in details (title, date/time, settings)
4. Save - students get notified
5. At scheduled time, click "Start Class"
6. Opens Jitsi meeting room as moderator
7. Students can join
8. Click "End Class" when done

### Student Workflow:
1. Navigate to class detail page
2. See "Live Classes" section
3. View scheduled/live classes
4. Click "Join" on live class
5. Opens Jitsi meeting room as participant
6. Participate in class
7. Leave when done

## Jitsi Configuration:

### Using Public Jitsi Server:
```typescript
const domain = 'meet.jit.si';
const roomName = `elearnhub-${meetingId}`;
```

### Using Self-Hosted Jitsi (Recommended for Production):
```typescript
const domain = 'your-jitsi-server.com';
const roomName = meetingId;
```

### Security Options:
1. **JWT Authentication** - Secure room access
2. **Password Protection** - Room passwords
3. **Lobby Mode** - Waiting room for participants
4. **Moderator Controls** - Only host can start

## Notifications:

Students receive notifications when:
- Live class is scheduled
- Live class is about to start (15 min before)
- Live class has started
- Live class recording is available

## Database Operations:

### When Scheduling:
1. Create LiveClass record
2. Generate unique meeting ID
3. Set status to SCHEDULED
4. Notify all enrolled students

### When Starting:
1. Update status to LIVE
2. Set actual_start_time
3. Notify students (class is live)

### When Ending:
1. Update status to ENDED
2. Set actual_end_time
3. Save recording URL (if recorded)

## UI/UX Considerations:

### Teacher View:
- Dashboard showing all scheduled classes
- Quick start button for live classes
- Participant count
- Recording status
- End class button

### Student View:
- List of upcoming classes
- "Live Now" indicator
- One-click join
- Class details (time, duration, host)

### Meeting Room:
- Full-screen Jitsi interface
- Minimal custom UI
- Exit button
- Class info header

## Testing Checklist:

### Backend:
- [ ] Create live class
- [ ] Update live class
- [ ] Cancel live class
- [ ] Start live class
- [ ] End live class
- [ ] Get live classes list
- [ ] Join live class

### Frontend:
- [ ] Schedule form works
- [ ] List displays correctly
- [ ] Start button works
- [ ] Jitsi loads properly
- [ ] Join as student works
- [ ] Video/audio works
- [ ] Screen share works
- [ ] Chat works
- [ ] End class works

### Integration:
- [ ] Notifications sent
- [ ] Status updates correctly
- [ ] Multiple participants
- [ ] Mobile devices
- [ ] Different browsers

## Deployment:

### Development:
- Use public Jitsi server (meet.jit.si)
- No additional setup needed

### Production:
1. **Option 1: Use Public Jitsi**
   - Free and easy
   - Shared infrastructure
   - Limited customization

2. **Option 2: Self-Host Jitsi**
   - Full control
   - Better performance
   - Custom branding
   - Requires server setup

### Self-Hosting Jitsi (Quick Guide):
```bash
# Install on Ubuntu 20.04+
wget -qO - https://download.jitsi.org/jitsi-key.gpg.key | sudo apt-key add -
sudo sh -c "echo 'deb https://download.jitsi.org stable/' > /etc/apt/sources.list.d/jitsi-stable.list"
sudo apt update
sudo apt install jitsi-meet
```

## Performance:

### Jitsi Scalability:
- 2-4 participants: Excellent
- 5-10 participants: Very Good
- 10-50 participants: Good
- 50-100 participants: Fair (needs good server)
- 100+ participants: Requires JVB cluster

### Bandwidth Requirements:
- Video (720p): ~1.5 Mbps per stream
- Video (360p): ~0.5 Mbps per stream
- Audio only: ~50 Kbps per stream

## Security:

1. **Authentication:**
   - JWT token validation
   - User must be enrolled in class

2. **Authorization:**
   - Only teacher can start/end
   - Only enrolled students can join

3. **Meeting Security:**
   - Unique meeting IDs
   - Optional passwords
   - Lobby mode available

## Cost Analysis:

### Using Public Jitsi:
- **Cost:** $0
- **Pros:** Free, no setup
- **Cons:** Shared resources, no SLA

### Self-Hosting:
- **Server:** $20-100/month (depending on size)
- **Bandwidth:** Variable
- **Maintenance:** Time investment
- **Total:** $20-100/month + time

### Commercial Alternatives:
- **Zoom SDK:** $100-2000/month
- **Agora.io:** Pay per use (~$1/1000 minutes)
- **Twilio Video:** Pay per use (~$0.004/minute/participant)

## Next Steps:

1. ✅ Run SQL script to create tables
2. ⏳ Create frontend components
3. ⏳ Integrate Jitsi
4. ⏳ Test with multiple users
5. ⏳ Add to class detail pages
6. ⏳ Deploy and test

## Estimated Timeline:

- Backend: ✅ COMPLETED (2-3 hours)
- Frontend Components: 3-4 hours
- Jitsi Integration: 2-3 hours
- Testing & Refinement: 2-3 hours
- **Total Remaining: 7-10 hours**

## Resources:

- Jitsi Meet: https://jitsi.org/
- Jitsi API Docs: https://jitsi.github.io/handbook/
- React SDK: https://github.com/jitsi/jitsi-meet-react-sdk
- IFrame API: https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe
