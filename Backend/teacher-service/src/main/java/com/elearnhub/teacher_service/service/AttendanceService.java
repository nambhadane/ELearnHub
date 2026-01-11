package com.elearnhub.teacher_service.service;

import com.elearnhub.teacher_service.dto.AttendanceSessionDTO;
import com.elearnhub.teacher_service.dto.AttendanceRecordDTO;
import com.elearnhub.teacher_service.dto.AttendanceStatisticsDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface AttendanceService {
    
    // Session Management
    AttendanceSessionDTO createSession(AttendanceSessionDTO sessionDTO);
    AttendanceSessionDTO updateSession(Long id, AttendanceSessionDTO sessionDTO);
    void deleteSession(Long id);
    AttendanceSessionDTO getSessionById(Long id);
    List<AttendanceSessionDTO> getSessionsByClass(Long classId);
    List<AttendanceSessionDTO> getSessionsByClassAndDateRange(Long classId, LocalDate startDate, LocalDate endDate);
    
    // Attendance Marking
    AttendanceRecordDTO markAttendance(Long sessionId, Long studentId, String status, Long markedBy, String notes);
    List<AttendanceRecordDTO> markBulkAttendance(Long sessionId, Map<Long, String> studentStatusMap, Long markedBy);
    AttendanceRecordDTO updateAttendanceRecord(Long recordId, String status, String notes);
    
    // Attendance Records
    List<AttendanceRecordDTO> getRecordsBySession(Long sessionId);
    List<AttendanceRecordDTO> getRecordsByStudent(Long studentId);
    List<AttendanceRecordDTO> getRecordsByClassAndStudent(Long classId, Long studentId);
    
    // Statistics
    AttendanceStatisticsDTO getStudentStatistics(Long classId, Long studentId);
    List<AttendanceStatisticsDTO> getClassStatistics(Long classId);
    Map<String, Object> getSessionStatistics(Long sessionId);
}
