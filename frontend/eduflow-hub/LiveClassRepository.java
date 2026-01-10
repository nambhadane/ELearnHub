package com.elearnhub.teacher_service.repository;

import com.elearnhub.teacher_service.entity.LiveClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LiveClassRepository extends JpaRepository<LiveClass, Long> {
    
    List<LiveClass> findByClassIdOrderByScheduledStartTimeDesc(Long classId);
    
    List<LiveClass> findByClassIdAndStatus(Long classId, String status);
    
    Optional<LiveClass> findByMeetingId(String meetingId);
    
    List<LiveClass> findByHostIdOrderByScheduledStartTimeDesc(Long hostId);
    
    List<LiveClass> findByStatusAndScheduledStartTimeBefore(String status, LocalDateTime time);
    
    List<LiveClass> findByStatusAndScheduledEndTimeBefore(String status, LocalDateTime time);
}
