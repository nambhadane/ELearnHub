# Live Class/Video Conferencing Implementation Plan

## Overview
Implementing a live video conferencing system for E-Learn Hub using WebRTC technology.

## Technology Stack

### Backend
- Spring Boot (existing)
- WebSocket for signaling
- STOMP protocol for messaging

### Frontend
- React + TypeScript
- WebRTC API (built into browsers)
- Simple-peer library (WebRTC wrapper)
- Socket.io-client or SockJS for WebSocket

### Video/Audio
- WebRTC (Peer-to-Peer)
- MediaStream API
- RTCPeerConnection

## Architecture

### Option 1: Peer-to-Peer (P2P) - Simple Implementation
**Pros:**
- No media server needed
- Lower server costs
- Direct communication
- Lower latency

**Cons:**
- Limited scalability (max ~10 participants)
- Higher bandwidth for each client
- Quality depends on client connections

### Option 2: SFU (Selective Forwarding Unit) - Production Ready
**Pros:**
- Scalable to 100+ participants
- Lower client bandwidth
- Better quality control

**Cons:**
- Requires media server (Janus, Mediasoup, Jitsi)
- More complex setup
- Higher server costs

### Recommendation: Start with Option 1 (P2P), migrate to Option 2 later

## Implementation Phases

### Phase 1: Basic Infrastructure (Current)
1. ✅ Database schema
2. ✅ Backend entities
3. ⏳ Repository, Service, Controller
4. ⏳ WebSocket configuration
5. ⏳ Signaling server

### Phase 2: Core Features
1. Schedule live class (teacher)
2. Start live class (teacher)
3. Join live class (students)
4. Video/Audio streaming
5. Mute/Unmute controls
6. Camera on/off

### Phase 3: Enhanced Features
1. Screen sharing
2. Chat functionality
3. Participant list
4. Hand raise
5. Kick participant (teacher)

### Phase 4: Advanced Features
1. Recording
2. Breakout rooms
3. Whiteboard
4. Polls
5. File sharing during class

## Database Schema

### live_classes table
```sql
- id (PK)
- class_id (FK to class_entity)
- title
- description
- scheduled_start_time
- scheduled_end_time
- actual_start_time
- actual_end_time
- status (SCHEDULED, LIVE, ENDED, CANCELLED)
- meeting_id (unique)
- meeting_password
- host_id (FK to users)
- recording_url
- allow_recording
- allow_chat
- allow_screen_share
- max_participants
- created_at, updated_at
```

### live_class_participants table
```sql
- id (PK)
- live_class_id (FK)
- user_id (FK)
- joined_at
- left_at
- duration_minutes
```

## API Endpoints

### Teacher Endpoints
- `POST /live-classes` - Schedule live class
- `PUT /live-classes/{id}` - Update live class
- `DELETE /live-classes/{id}` - Cancel live class
- `POST /live-classes/{id}/start` - Start live class
- `POST /live-classes/{id}/end` - End live class
- `GET /live-classes/class/{classId}` - Get class live sessions
- `POST /live-classes/{id}/kick/{userId}` - Kick participant

### Student Endpoints
- `GET /live-classes/available/class/{classId}` - Get available live classes
- `POST /live-classes/{id}/join` - Join live class
- `POST /live-classes/{id}/leave` - Leave live class

### WebSocket Endpoints
- `/ws/live-class/{meetingId}` - WebSocket connection
- `/topic/live-class/{meetingId}/signal` - Signaling messages
- `/topic/live-class/{meetingId}/chat` - Chat messages
- `/topic/live-class/{meetingId}/participants` - Participant updates

## WebRTC Signaling Flow

1. **Teacher starts class:**
   - Creates meeting room
   - Generates meeting ID
   - Connects to WebSocket
   - Initializes local media stream

2. **Student joins:**
   - Connects to WebSocket with meeting ID
   - Sends "join" signal
   - Receives list of existing participants

3. **Peer Connection:**
   - Student creates RTCPeerConnection for each participant
   - Exchanges ICE candidates via signaling server
   - Exchanges SDP offers/answers
   - Establishes P2P connection

4. **Media Streaming:**
   - Local media stream added to peer connections
   - Remote streams received and displayed
   - Audio/Video tracks managed

## Frontend Components

### Teacher Components
- `LiveClassScheduler` - Schedule new live class
- `LiveClassList` - View scheduled classes
- `LiveClassRoom` - Main video conferencing interface (host)
- `LiveClassControls` - Host controls (mute all, end class, etc.)

### Student Components
- `AvailableLiveClasses` - View upcoming/live classes
- `LiveClassRoom` - Main video conferencing interface (participant)
- `LiveClassControls` - Participant controls (mute, camera, etc.)

### Shared Components
- `VideoGrid` - Display participant videos
- `VideoTile` - Individual participant video
- `ChatPanel` - Chat interface
- `ParticipantsList` - List of participants
- `ScreenShare` - Screen sharing display

## WebRTC Implementation

### Key Classes/Hooks
```typescript
// useWebRTC.ts - Custom hook for WebRTC logic
- initializeMedia() - Get camera/mic access
- createPeerConnection() - Create RTCPeerConnection
- handleOffer() - Handle SDP offer
- handleAnswer() - Handle SDP answer
- handleIceCandidate() - Handle ICE candidate
- addTrack() - Add local track to connection
- removeTrack() - Remove track
- toggleAudio() - Mute/unmute
- toggleVideo() - Camera on/off
- startScreenShare() - Share screen
- stopScreenShare() - Stop sharing
```

### Signaling Messages
```typescript
{
  type: 'join' | 'leave' | 'offer' | 'answer' | 'ice-candidate' | 'chat',
  from: userId,
  to: userId, // for direct messages
  data: any
}
```

## Security Considerations

1. **Authentication:**
   - JWT token validation for WebSocket
   - Verify user is enrolled in class

2. **Authorization:**
   - Only teacher can start/end class
   - Only teacher can kick participants
   - Only enrolled students can join

3. **Meeting Security:**
   - Unique meeting IDs
   - Optional meeting passwords
   - Waiting room (optional)

4. **Data Protection:**
   - Encrypted WebSocket (WSS)
   - HTTPS for all API calls
   - No recording without consent

## Performance Optimization

1. **Video Quality:**
   - Adaptive bitrate
   - Resolution scaling based on bandwidth
   - Simulcast for multiple quality levels

2. **Connection:**
   - TURN server fallback for NAT traversal
   - Connection quality monitoring
   - Automatic reconnection

3. **UI:**
   - Virtual scrolling for participant list
   - Lazy loading for video tiles
   - Optimized re-renders

## Testing Strategy

1. **Unit Tests:**
   - Service layer tests
   - WebSocket message handling
   - Signaling logic

2. **Integration Tests:**
   - API endpoint tests
   - Database operations
   - WebSocket connections

3. **E2E Tests:**
   - Complete user flows
   - Multi-participant scenarios
   - Network condition simulation

## Deployment Considerations

1. **STUN/TURN Servers:**
   - Use public STUN servers (Google, etc.)
   - Set up TURN server for production (coturn)

2. **WebSocket:**
   - Configure WebSocket in production
   - Load balancing with sticky sessions
   - SSL/TLS certificates

3. **Scaling:**
   - Horizontal scaling with session affinity
   - Redis for WebSocket message broker
   - Consider SFU for large classes

## Limitations (P2P Approach)

1. **Participant Limit:**
   - Recommended: 4-6 participants
   - Maximum: 10-12 participants
   - Beyond this, quality degrades

2. **Bandwidth:**
   - Each participant needs upload bandwidth for all others
   - N*(N-1) connections for N participants

3. **Solution:**
   - Implement SFU for larger classes
   - Use third-party services (Jitsi, Zoom SDK)

## Alternative: Third-Party Integration

If P2P limitations are too restrictive, consider:

1. **Jitsi Meet:**
   - Open source
   - Self-hosted or cloud
   - Scalable
   - Free

2. **Agora.io:**
   - Commercial
   - Excellent quality
   - Easy integration
   - Pay per use

3. **Zoom SDK:**
   - Commercial
   - Familiar interface
   - Reliable
   - Higher cost

## Next Steps

1. Implement WebSocket configuration
2. Create signaling server
3. Build basic video room UI
4. Test with 2-3 participants
5. Add chat and controls
6. Implement screen sharing
7. Add recording (optional)
8. Performance testing
9. Production deployment

## Estimated Timeline

- Phase 1 (Infrastructure): 2-3 days
- Phase 2 (Core Features): 3-4 days
- Phase 3 (Enhanced Features): 2-3 days
- Phase 4 (Advanced Features): 3-5 days
- Testing & Refinement: 2-3 days

**Total: 12-18 days for complete implementation**

## Resources Needed

1. **Development:**
   - WebRTC knowledge
   - WebSocket experience
   - React hooks expertise

2. **Infrastructure:**
   - STUN server (free)
   - TURN server (for production)
   - SSL certificates
   - Increased server resources

3. **Testing:**
   - Multiple devices/browsers
   - Different network conditions
   - Load testing tools

## Success Metrics

1. **Technical:**
   - < 500ms latency
   - > 95% connection success rate
   - < 5% packet loss
   - Supports 6+ participants smoothly

2. **User Experience:**
   - Easy to join (< 3 clicks)
   - Intuitive controls
   - Stable connections
   - Good audio/video quality

3. **Business:**
   - Increased class engagement
   - Reduced need for physical meetings
   - Better learning outcomes
   - Positive user feedback
