package com.elearnhub.teacher_service.repository;

import com.elearnhub.teacher_service.entity.AttendanceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {
    
    List<AttendanceSession> findByClassIdOrderBySessionDateDesc(Long classId);
    
    List<AttendanceSession> findByClassIdAndSessionDateBetween(
            Long classId, LocalDate startDate, LocalDate endDate);
    
    Optional<AttendanceSession> findByClassIdAndSessionDateAndTitle(
            Long classId, LocalDate sessionDate, String title);
    
    @Query("SELECT COUNT(s) FROM AttendanceSession s WHERE s.classId = :classId")
    Long countByClassId(@Param("classId") Long classId);
    
    @Query("SELECT s FROM AttendanceSession s LEFT JOIN FETCH s.records WHERE s.id = :id")
    Optional<AttendanceSession> findByIdWithRecords(@Param("id") Long id);
}
