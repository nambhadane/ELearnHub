import { useEffect, useRef } from 'react';

declare global {
  interface Window {
    JitsiMeetExternalAPI: any;
  }
}

interface JitsiMeetingProps {
  roomName: string;
  displayName: string;
  onMeetingEnd?: () => void;
  isModerator?: boolean;
}

export function JitsiMeeting({ roomName, displayName, onMeetingEnd, isModerator = false }: JitsiMeetingProps) {
  const jitsiContainerRef = useRef<HTMLDivElement>(null);
  const jitsiApiRef = useRef<any>(null);

  useEffect(() => {
    // Load Jitsi Meet External API script
    const script = document.createElement('script');
    script.src = 'https://meet.jit.si/external_api.js';
    script.async = true;
    script.onload = () => initializeJitsi();
    document.body.appendChild(script);

    return () => {
      // Cleanup
      if (jitsiApiRef.current) {
        jitsiApiRef.current.dispose();
      }
      document.body.removeChild(script);
    };
  }, []);

  const initializeJitsi = () => {
    if (!jitsiContainerRef.current || !window.JitsiMeetExternalAPI) return;

    const domain = 'meet.jit.si';
    
    const options = {
      roomName: roomName, // Use the meetingId directly
      width: '100%',
      height: '100%',
      parentNode: jitsiContainerRef.current,
      configOverwrite: {
        startWithAudioMuted: false,
        startWithVideoMuted: false,
        enableWelcomePage: false,
        prejoinPageEnabled: false,
        disableDeepLinking: true,
      },
      interfaceConfigOverwrite: {
        TOOLBAR_BUTTONS: [
          'microphone',
          'camera',
          'desktop',
          'fullscreen',
          'hangup',
          'chat',
          'settings',
          'raisehand',
          'videoquality',
          'filmstrip',
          'tileview',
        ],
        SHOW_JITSI_WATERMARK: false,
        SHOW_WATERMARK_FOR_GUESTS: false,
        DEFAULT_REMOTE_DISPLAY_NAME: 'Participant',
      },
      userInfo: {
        displayName: displayName,
      },
    };

    jitsiApiRef.current = new window.JitsiMeetExternalAPI(domain, options);

    // Event listeners
    jitsiApiRef.current.addEventListener('videoConferenceJoined', () => {
      console.log('User joined the conference');
    });

    jitsiApiRef.current.addEventListener('videoConferenceLeft', () => {
      console.log('User left the conference');
      if (onMeetingEnd) {
        onMeetingEnd();
      }
    });

    jitsiApiRef.current.addEventListener('readyToClose', () => {
      console.log('Meeting ended');
      if (onMeetingEnd) {
        onMeetingEnd();
      }
    });
  };

  return (
    <div 
      ref={jitsiContainerRef} 
      style={{ 
        width: '100%', 
        height: '100vh',
        position: 'fixed',
        top: 0,
        left: 0,
        zIndex: 9999
      }} 
    />
  );
}
