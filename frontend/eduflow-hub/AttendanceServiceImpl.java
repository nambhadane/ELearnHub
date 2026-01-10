package com.elearnhub.teacher_service.service;

import com.elearnhub.teacher_service.dto.AttendanceSessionDTO;
import com.elearnhub.teacher_service.dto.AttendanceRecordDTO;
import com.elearnhub.teacher_service.dto.AttendanceStatisticsDTO;
import com.elearnhub.teacher_service.entity.AttendanceSession;
import com.elearnhub.teacher_service.entity.AttendanceRecord;
import com.elearnhub.teacher_service.entity.ClassEntity;
import com.elearnhub.teacher_service.entity.User;
import com.elearnhub.teacher_service.repository.AttendanceSessionRepository;
import com.elearnhub.teacher_service.repository.AttendanceRecordRepository;
import com.elearnhub.teacher_service.repository.ClassRepository;
import com.elearnhub.teacher_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AttendanceServiceImpl implements AttendanceService {

    @Autowired
    private AttendanceSessionRepository sessionRepository;

    @Autowired
    private AttendanceRecordRepository recordRepository;

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private UserRepository userRepository;

    // ============================================
    // Session Management
    // ============================================

    @Override
    public AttendanceSessionDTO createSession(AttendanceSessionDTO sessionDTO) {
        // Validate class exists
        ClassEntity classEntity = classRepository.findById(sessionDTO.getClassId())
                .orElseThrow(() -> new RuntimeException("Class not found"));

        // Create session entity
        AttendanceSession session = new AttendanceSession();
        session.setClassId(sessionDTO.getClassId());
        session.setSessionDate(sessionDTO.getSessionDate());
        session.setSessionTime(sessionDTO.getSessionTime());
        session.setTitle(sessionDTO.getTitle());
        session.setDescription(sessionDTO.getDescription());
        session.setCreatedBy(sessionDTO.getCreatedBy());
        session.setCreatedAt(LocalDateTime.now());

        // Save session
        session = sessionRepository.save(session);

        // Convert to DTO and return
        return convertToSessionDTO(session);
    }

    @Override
    public AttendanceSessionDTO updateSession(Long id, AttendanceSessionDTO sessionDTO) {
        AttendanceSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        // Update fields
        session.setSessionDate(sessionDTO.getSessionDate());
        session.setSessionTime(sessionDTO.getSessionTime());
        session.setTitle(sessionDTO.getTitle());
        session.setDescription(sessionDTO.getDescription());
        session.setUpdatedAt(LocalDateTime.now());

        // Save and return
        session = sessionRepository.save(session);
        return convertToSessionDTO(session);
    }

    @Override
    public void deleteSession(Long id) {
        if (!sessionRepository.existsById(id)) {
            throw new RuntimeException("Session not found");
        }
        sessionRepository.deleteById(id);
    }

    @Override
    public AttendanceSessionDTO getSessionById(Long id) {
        AttendanceSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        return convertToSessionDTO(session);
    }

    @Override
    public List<AttendanceSessionDTO> getSessionsByClass(Long classId) {
        List<AttendanceSession> sessions = sessionRepository.findByClassIdOrderBySessionDateDesc(classId);
        return sessions.stream()
                .map(this::convertToSessionDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceSessionDTO> getSessionsByClassAndDateRange(Long classId, LocalDate startDate, LocalDate endDate) {
        List<AttendanceSession> sessions = sessionRepository.findByClassIdAndSessionDateBetween(classId, startDate, endDate);
        return sessions.stream()
                .map(this::convertToSessionDTO)
                .collect(Collectors.toList());
    }

    // ============================================
    // Attendance Marking
    // ============================================

    @Override
    public AttendanceRecordDTO markAttendance(Long sessionId, Long studentId, String status, Long markedBy, String notes) {
        // Validate session exists
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        // Validate student exists
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // Check if record already exists
        Optional<AttendanceRecord> existingRecord = recordRepository.findBySessionIdAndStudentId(sessionId, studentId);

        AttendanceRecord record;
        if (existingRecord.isPresent()) {
            // Update existing record
            record = existingRecord.get();
            record.setStatus(status);
            record.setMarkedAt(LocalDateTime.now());
            record.setMarkedBy(markedBy);
            if (notes != null) {
                record.setNotes(notes);
            }
        } else {
            // Create new record
            record = new AttendanceRecord();
            record.setSession(session);
            record.setSessionId(sessionId);
            record.setStudentId(studentId);
            record.setStatus(status);
            record.setMarkedAt(LocalDateTime.now());
            record.setMarkedBy(markedBy);
            record.setNotes(notes);
        }

        // Save and return
        record = recordRepository.save(record);
        return convertToRecordDTO(record);
    }

    @Override
    public List<AttendanceRecordDTO> markBulkAttendance(Long sessionId, Map<Long, String> studentStatusMap, Long markedBy) {
        // Validate session exists
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        List<AttendanceRecord> records = new ArrayList<>();

        for (Map.Entry<Long, String> entry : studentStatusMap.entrySet()) {
            Long studentId = entry.getKey();
            String status = entry.getValue();

            // Check if record exists
            Optional<AttendanceRecord> existingRecord = recordRepository.findBySessionIdAndStudentId(sessionId, studentId);

            AttendanceRecord record;
            if (existingRecord.isPresent()) {
                record = existingRecord.get();
                record.setStatus(status);
                record.setMarkedAt(LocalDateTime.now());
                record.setMarkedBy(markedBy);
            } else {
                record = new AttendanceRecord();
                record.setSession(session);
                record.setSessionId(sessionId);
                record.setStudentId(studentId);
                record.setStatus(status);
                record.setMarkedAt(LocalDateTime.now());
                record.setMarkedBy(markedBy);
            }

            records.add(record);
        }

        // Save all records
        records = recordRepository.saveAll(records);

        // Convert to DTOs
        return records.stream()
                .map(this::convertToRecordDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AttendanceRecordDTO updateAttendanceRecord(Long recordId, String status, String notes) {
        AttendanceRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Attendance record not found"));

        record.setStatus(status);
        if (notes != null) {
            record.setNotes(notes);
        }
        record.setMarkedAt(LocalDateTime.now());

        record = recordRepository.save(record);
        return convertToRecordDTO(record);
    }

    // ============================================
    // Attendance Records
    // ============================================

    @Override
    public List<AttendanceRecordDTO> getRecordsBySession(Long sessionId) {
        List<AttendanceRecord> records = recordRepository.findBySessionId(sessionId);
        return records.stream()
                .map(this::convertToRecordDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceRecordDTO> getRecordsByStudent(Long studentId) {
        List<AttendanceRecord> records = recordRepository.findByStudentId(studentId);
        return records.stream()
                .map(this::convertToRecordDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceRecordDTO> getRecordsByClassAndStudent(Long classId, Long studentId) {
        List<AttendanceRecord> records = recordRepository.findByClassIdAndStudentId(classId, studentId);
        return records.stream()
                .map(this::convertToRecordDTO)
                .collect(Collectors.toList());
    }

    // ============================================
    // Statistics
    // ============================================

    @Override
    public AttendanceStatisticsDTO getStudentStatistics(Long classId, Long studentId) {
        List<AttendanceRecord> records = recordRepository.findByClassIdAndStudentId(classId, studentId);

        AttendanceStatisticsDTO stats = new AttendanceStatisticsDTO();
        stats.setStudentId(studentId);
        stats.setClassId(classId);

        // Get student name
        userRepository.findById(studentId).ifPresent(user -> {
            stats.setStudentName(user.getName());
        });

        // Calculate statistics
        int totalSessions = records.size();
        long presentCount = records.stream().filter(r -> "PRESENT".equals(r.getStatus())).count();
        long absentCount = records.stream().filter(r -> "ABSENT".equals(r.getStatus())).count();
        long lateCount = records.stream().filter(r -> "LATE".equals(r.getStatus())).count();

        stats.setTotalSessions(totalSessions);
        stats.setPresentCount((int) presentCount);
        stats.setAbsentCount((int) absentCount);
        stats.setLateCount((int) lateCount);

        // Calculate percentage
        if (totalSessions > 0) {
            double percentage = (double) presentCount / totalSessions * 100;
            stats.setAttendancePercentage(Math.round(percentage * 100.0) / 100.0);
        } else {
            stats.setAttendancePercentage(0.0);
        }

        return stats;
    }

    @Override
    public List<AttendanceStatisticsDTO> getClassStatistics(Long classId) {
        // Get all students in the class
        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        List<AttendanceStatisticsDTO> statsList = new ArrayList<>();

        if (classEntity.getStudents() != null) {
            for (User student : classEntity.getStudents()) {
                AttendanceStatisticsDTO stats = getStudentStatistics(classId, student.getId());
                statsList.add(stats);
            }
        }

        return statsList;
    }

    @Override
    public Map<String, Object> getSessionStatistics(Long sessionId) {
        List<AttendanceRecord> records = recordRepository.findBySessionId(sessionId);

        Map<String, Object> stats = new HashMap<>();

        int totalStudents = records.size();
        long presentCount = records.stream().filter(r -> "PRESENT".equals(r.getStatus())).count();
        long absentCount = records.stream().filter(r -> "ABSENT".equals(r.getStatus())).count();
        long lateCount = records.stream().filter(r -> "LATE".equals(r.getStatus())).count();

        stats.put("totalStudents", totalStudents);
        stats.put("presentCount", presentCount);
        stats.put("absentCount", absentCount);
        stats.put("lateCount", lateCount);

        if (totalStudents > 0) {
            double percentage = (double) presentCount / totalStudents * 100;
            stats.put("attendancePercentage", Math.round(percentage * 100.0) / 100.0);
        } else {
            stats.put("attendancePercentage", 0.0);
        }

        return stats;
    }

    // ============================================
    // Helper Methods
    // ============================================

    private AttendanceSessionDTO convertToSessionDTO(AttendanceSession session) {
        AttendanceSessionDTO dto = new AttendanceSessionDTO();
        dto.setId(session.getId());
        dto.setClassId(session.getClassId());
        dto.setSessionDate(session.getSessionDate());
        dto.setSessionTime(session.getSessionTime());
        dto.setTitle(session.getTitle());
        dto.setDescription(session.getDescription());
        dto.setCreatedBy(session.getCreatedBy());
        dto.setCreatedAt(session.getCreatedAt());
        dto.setUpdatedAt(session.getUpdatedAt());

        // Get creator name
        userRepository.findById(session.getCreatedBy()).ifPresent(user -> {
            dto.setCreatedByName(user.getName());
        });

        // Get records and calculate statistics
        List<AttendanceRecord> records = recordRepository.findBySessionId(session.getId());
        
        int totalStudents = records.size();
        long presentCount = records.stream().filter(r -> "PRESENT".equals(r.getStatus())).count();
        long absentCount = records.stream().filter(r -> "ABSENT".equals(r.getStatus())).count();
        long lateCount = records.stream().filter(r -> "LATE".equals(r.getStatus())).count();

        dto.setTotalStudents(totalStudents);
        dto.setPresentCount((int) presentCount);
        dto.setAbsentCount((int) absentCount);
        dto.setLateCount((int) lateCount);

        if (totalStudents > 0) {
            double percentage = (double) presentCount / totalStudents * 100;
            dto.setAttendancePercentage(Math.round(percentage * 100.0) / 100.0);
        } else {
            dto.setAttendancePercentage(0.0);
        }

        // Convert records to DTOs
        List<AttendanceRecordDTO> recordDTOs = records.stream()
                .map(this::convertToRecordDTO)
                .collect(Collectors.toList());
        dto.setRecords(recordDTOs);

        return dto;
    }

    private AttendanceRecordDTO convertToRecordDTO(AttendanceRecord record) {
        AttendanceRecordDTO dto = new AttendanceRecordDTO();
        dto.setId(record.getId());
        dto.setSessionId(record.getSessionId());
        dto.setStudentId(record.getStudentId());
        dto.setStatus(record.getStatus());
        dto.setMarkedAt(record.getMarkedAt());
        dto.setMarkedBy(record.getMarkedBy());
        dto.setNotes(record.getNotes());

        // Get student name
        userRepository.findById(record.getStudentId()).ifPresent(user -> {
            dto.setStudentName(user.getName());
        });

        // Get marker name
        userRepository.findById(record.getMarkedBy()).ifPresent(user -> {
            dto.setMarkedByName(user.getName());
        });

        return dto;
    }
}
