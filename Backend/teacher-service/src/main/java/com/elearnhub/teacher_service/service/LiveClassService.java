package com.elearnhub.teacher_service.service;

import com.elearnhub.teacher_service.dto.LiveClassDTO;

import java.util.List;

public interface LiveClassService {
    
    // Teacher operations
    LiveClassDTO scheduleLiveClass(LiveClassDTO liveClassDTO);
    LiveClassDTO updateLiveClass(Long id, LiveClassDTO liveClassDTO);
    void cancelLiveClass(Long id);
    LiveClassDTO startLiveClass(Long id, Long teacherId);
    LiveClassDTO endLiveClass(Long id, Long teacherId);
    List<LiveClassDTO> getLiveClassesByClass(Long classId);
    List<LiveClassDTO> getTeacherLiveClasses(Long teacherId);
    
    // Student operations
    List<LiveClassDTO> getAvailableLiveClasses(Long classId);
    LiveClassDTO joinLiveClass(Long id, Long studentId);
    void leaveLiveClass(Long id, Long studentId);
    
    // Common operations
    LiveClassDTO getLiveClassById(Long id);
    LiveClassDTO getLiveClassByMeetingId(String meetingId);
}
