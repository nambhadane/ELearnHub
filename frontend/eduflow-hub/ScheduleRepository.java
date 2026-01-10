package com.elearnhub.teacher_service.repository;

import com.elearnhub.teacher_service.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByClassIdOrderByDayOfWeekAscStartTimeAsc(Long classId);
    List<Schedule> findByClassIdAndDayOfWeek(Long classId, String dayOfWeek);
}
