package com.elearnhub.teacher_service.repository;

import com.elearnhub.teacher_service.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    
    List<AttendanceRecord> findBySessionId(Long sessionId);
    
    List<AttendanceRecord> findByStudentId(Long studentId);
    
    Optional<AttendanceRecord> findBySessionIdAndStudentId(Long sessionId, Long studentId);
    
    @Query("SELECT ar FROM AttendanceRecord ar " +
           "JOIN AttendanceSession s ON ar.sessionId = s.id " +
           "WHERE s.classId = :classId AND ar.studentId = :studentId " +
           "ORDER BY s.sessionDate DESC")
    List<AttendanceRecord> findByClassIdAndStudentId(
            @Param("classId") Long classId, 
            @Param("studentId") Long studentId);
    
    @Query("SELECT COUNT(ar) FROM AttendanceRecord ar " +
           "WHERE ar.sessionId = :sessionId AND ar.status = :status")
    Long countBySessionIdAndStatus(
            @Param("sessionId") Long sessionId, 
            @Param("status") String status);
    
    @Query("SELECT ar.status, COUNT(ar) FROM AttendanceRecord ar " +
           "WHERE ar.sessionId = :sessionId GROUP BY ar.status")
    List<Object[]> getStatusCountsBySessionId(@Param("sessionId") Long sessionId);
}
