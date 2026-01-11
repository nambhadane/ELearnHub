package com.elearnhub.teacher_service.service;

import com.elearnhub.teacher_service.dto.ScheduleDTO;
import com.elearnhub.teacher_service.entity.Schedule;

import java.util.List;

public interface ScheduleService {
    Schedule createSchedule(Schedule schedule);
    Schedule updateSchedule(Long id, Schedule schedule);
    void deleteSchedule(Long id);
    Schedule getScheduleById(Long id);
    List<ScheduleDTO> getSchedulesByClassId(Long classId);
    List<ScheduleDTO> getSchedulesByStudentId(Long studentId);
}
