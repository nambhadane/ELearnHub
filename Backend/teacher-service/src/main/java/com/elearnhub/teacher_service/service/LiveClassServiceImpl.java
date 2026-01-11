package com.elearnhub.teacher_service.service;

import com.elearnhub.teacher_service.dto.LiveClassDTO;
import com.elearnhub.teacher_service.entity.ClassEntity;
import com.elearnhub.teacher_service.entity.LiveClass;
import com.elearnhub.teacher_service.entity.Notification;
import com.elearnhub.teacher_service.entity.User;
import com.elearnhub.teacher_service.repository.ClassEntityRepository;
//import com.elearnhub.teacher_service.repository.ClassRepository;
import com.elearnhub.teacher_service.repository.LiveClassRepository;
import com.elearnhub.teacher_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class LiveClassServiceImpl implements LiveClassService {

    @Autowired
    private LiveClassRepository liveClassRepository;

    @Autowired
    private ClassEntityRepository classRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Override
    public LiveClassDTO scheduleLiveClass(LiveClassDTO liveClassDTO) {
        // Validate class exists
        ClassEntity classEntity = classRepository.findById(liveClassDTO.getClassId())
                .orElseThrow(() -> new RuntimeException("Class not found"));

        // Create live class entity
        LiveClass liveClass = new LiveClass();
        liveClass.setClassId(liveClassDTO.getClassId());
        liveClass.setTitle(liveClassDTO.getTitle());
        liveClass.setDescription(liveClassDTO.getDescription());
        liveClass.setScheduledStartTime(liveClassDTO.getScheduledStartTime());
        liveClass.setScheduledEndTime(liveClassDTO.getScheduledEndTime());
        liveClass.setHostId(liveClassDTO.getHostId());
        liveClass.setStatus("SCHEDULED");
        
        // Generate unique meeting ID
        liveClass.setMeetingId(generateMeetingId());
        
        // Set optional fields
        liveClass.setAllowRecording(liveClassDTO.getAllowRecording() != null ? liveClassDTO.getAllowRecording() : false);
        liveClass.setAllowChat(liveClassDTO.getAllowChat() != null ? liveClassDTO.getAllowChat() : true);
        liveClass.setAllowScreenShare(liveClassDTO.getAllowScreenShare() != null ? liveClassDTO.getAllowScreenShare() : true);
        liveClass.setMaxParticipants(liveClassDTO.getMaxParticipants() != null ? liveClassDTO.getMaxParticipants() : 100);
        
        LiveClass savedLiveClass = liveClassRepository.save(liveClass);

        // Notify students
        notifyStudentsAboutLiveClass(savedLiveClass, classEntity);

        return convertToDTO(savedLiveClass);
    }

    @Override
    public LiveClassDTO updateLiveClass(Long id, LiveClassDTO liveClassDTO) {
        LiveClass liveClass = liveClassRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Live class not found"));

        // Only allow updates if not started
        if ("LIVE".equals(liveClass.getStatus()) || "ENDED".equals(liveClass.getStatus())) {
            throw new RuntimeException("Cannot update live or ended class");
        }

        liveClass.setTitle(liveClassDTO.getTitle());
        liveClass.setDescription(liveClassDTO.getDescription());
        liveClass.setScheduledStartTime(liveClassDTO.getScheduledStartTime());
        liveClass.setScheduledEndTime(liveClassDTO.getScheduledEndTime());
        liveClass.setAllowRecording(liveClassDTO.getAllowRecording());
        liveClass.setAllowChat(liveClassDTO.getAllowChat());
        liveClass.setAllowScreenShare(liveClassDTO.getAllowScreenShare());
        liveClass.setMaxParticipants(liveClassDTO.getMaxParticipants());
        liveClass.setUpdatedAt(LocalDateTime.now());

        LiveClass updatedLiveClass = liveClassRepository.save(liveClass);
        return convertToDTO(updatedLiveClass);
    }

    @Override
    public void cancelLiveClass(Long id) {
        LiveClass liveClass = liveClassRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Live class not found"));

        if ("LIVE".equals(liveClass.getStatus())) {
            throw new RuntimeException("Cannot cancel live class that is in progress");
        }

        liveClass.setStatus("CANCELLED");
        liveClass.setUpdatedAt(LocalDateTime.now());
        liveClassRepository.save(liveClass);
    }

    @Override
    public LiveClassDTO startLiveClass(Long id, Long teacherId) {
        LiveClass liveClass = liveClassRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Live class not found"));

        // Verify teacher is the host
        if (!liveClass.getHostId().equals(teacherId)) {
            throw new RuntimeException("Only the host can start the live class");
        }

        if (!"SCHEDULED".equals(liveClass.getStatus())) {
            throw new RuntimeException("Live class is not in scheduled status");
        }

        liveClass.setStatus("LIVE");
        liveClass.setActualStartTime(LocalDateTime.now());
        liveClass.setUpdatedAt(LocalDateTime.now());

        LiveClass updatedLiveClass = liveClassRepository.save(liveClass);
        return convertToDTO(updatedLiveClass);
    }

    @Override
    public LiveClassDTO endLiveClass(Long id, Long teacherId) {
        LiveClass liveClass = liveClassRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Live class not found"));

        // Verify teacher is the host
        if (!liveClass.getHostId().equals(teacherId)) {
            throw new RuntimeException("Only the host can end the live class");
        }

        if (!"LIVE".equals(liveClass.getStatus())) {
            throw new RuntimeException("Live class is not currently live");
        }

        liveClass.setStatus("ENDED");
        liveClass.setActualEndTime(LocalDateTime.now());
        liveClass.setUpdatedAt(LocalDateTime.now());

        LiveClass updatedLiveClass = liveClassRepository.save(liveClass);
        return convertToDTO(updatedLiveClass);
    }

    @Override
    public List<LiveClassDTO> getLiveClassesByClass(Long classId) {
        List<LiveClass> liveClasses = liveClassRepository.findByClassIdOrderByScheduledStartTimeDesc(classId);
        return liveClasses.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LiveClassDTO> getTeacherLiveClasses(Long teacherId) {
        List<LiveClass> liveClasses = liveClassRepository.findByHostIdOrderByScheduledStartTimeDesc(teacherId);
        return liveClasses.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LiveClassDTO> getAvailableLiveClasses(Long classId) {
        List<LiveClass> liveClasses = liveClassRepository.findByClassIdOrderByScheduledStartTimeDesc(classId);
        LocalDateTime now = LocalDateTime.now();

        return liveClasses.stream()
                .filter(lc -> ("SCHEDULED".equals(lc.getStatus()) || "LIVE".equals(lc.getStatus())) 
                        && !"CANCELLED".equals(lc.getStatus()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public LiveClassDTO joinLiveClass(Long id, Long studentId) {
        LiveClass liveClass = liveClassRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Live class not found"));

        if (!"LIVE".equals(liveClass.getStatus())) {
            throw new RuntimeException("Live class is not currently active");
        }

        // TODO: Track participant join in live_class_participants table

        return convertToDTO(liveClass);
    }

    @Override
    public void leaveLiveClass(Long id, Long studentId) {
        // TODO: Track participant leave in live_class_participants table
    }

    @Override
    public LiveClassDTO getLiveClassById(Long id) {
        LiveClass liveClass = liveClassRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Live class not found"));
        return convertToDTO(liveClass);
    }

    @Override
    public LiveClassDTO getLiveClassByMeetingId(String meetingId) {
        LiveClass liveClass = liveClassRepository.findByMeetingId(meetingId)
                .orElseThrow(() -> new RuntimeException("Live class not found"));
        return convertToDTO(liveClass);
    }

    // Helper methods

    private String generateMeetingId() {
        return "meet-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private void notifyStudentsAboutLiveClass(LiveClass liveClass, ClassEntity classEntity) {
        if (classEntity.getStudents() == null || classEntity.getStudents().isEmpty()) {
            return;
        }

        for (User student : classEntity.getStudents()) {
            try {
                notificationService.createNotification(
                        student.getId(),
                        "Live Class Scheduled",
                        "A live class '" + liveClass.getTitle() + "' is scheduled for " + 
                        liveClass.getScheduledStartTime().toString(),
                        Notification.NotificationType.ANNOUNCEMENT,
                        liveClass.getId(),
                        "LIVE_CLASS"
                );
            } catch (Exception e) {
                System.err.println("Failed to notify student " + student.getId() + ": " + e.getMessage());
            }
        }
    }

    private LiveClassDTO convertToDTO(LiveClass liveClass) {
        LiveClassDTO dto = new LiveClassDTO();
        dto.setId(liveClass.getId());
        dto.setClassId(liveClass.getClassId());
        dto.setTitle(liveClass.getTitle());
        dto.setDescription(liveClass.getDescription());
        dto.setScheduledStartTime(liveClass.getScheduledStartTime());
        dto.setScheduledEndTime(liveClass.getScheduledEndTime());
        dto.setActualStartTime(liveClass.getActualStartTime());
        dto.setActualEndTime(liveClass.getActualEndTime());
        dto.setStatus(liveClass.getStatus());
        dto.setMeetingId(liveClass.getMeetingId());
        dto.setMeetingPassword(liveClass.getMeetingPassword());
        dto.setHostId(liveClass.getHostId());
        dto.setRecordingUrl(liveClass.getRecordingUrl());
        dto.setAllowRecording(liveClass.getAllowRecording());
        dto.setAllowChat(liveClass.getAllowChat());
        dto.setAllowScreenShare(liveClass.getAllowScreenShare());
        dto.setMaxParticipants(liveClass.getMaxParticipants());
        dto.setCreatedAt(liveClass.getCreatedAt());

        // Fetch class name
        try {
            ClassEntity classEntity = classRepository.findById(liveClass.getClassId()).orElse(null);
            if (classEntity != null) {
                dto.setClassName(classEntity.getName());
            }
        } catch (Exception e) {
            // Ignore
        }

        // Fetch host name
        try {
            User host = userRepository.findById(liveClass.getHostId()).orElse(null);
            if (host != null) {
                dto.setHostName(host.getName());
            }
        } catch (Exception e) {
            // Ignore
        }

        return dto;
    }
}
