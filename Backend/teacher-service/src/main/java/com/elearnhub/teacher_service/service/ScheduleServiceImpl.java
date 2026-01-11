package com.elearnhub.teacher_service.service;

import com.elearnhub.teacher_service.dto.ScheduleDTO;
import com.elearnhub.teacher_service.entity.ClassEntity;
import com.elearnhub.teacher_service.entity.Course;
import com.elearnhub.teacher_service.entity.Schedule;
import com.elearnhub.teacher_service.repository.ClassEntityRepository;
//import com.elearnhub.teacher_service.repository.ClassRepository;
import com.elearnhub.teacher_service.repository.CourseRepository;
import com.elearnhub.teacher_service.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ScheduleServiceImpl implements ScheduleService {
    
    @Autowired
    private ScheduleRepository scheduleRepository;
    
    @Autowired
    private ClassEntityRepository classRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Override
    public Schedule createSchedule(Schedule schedule) {
        Schedule saved = scheduleRepository.save(schedule);
        System.out.println("✅ Schedule created for class " + schedule.getClassId() + 
                         " on " + schedule.getDayOfWeek());
        return saved;
    }
    
    @Override
    public Schedule updateSchedule(Long id, Schedule schedule) {
        Schedule existing = getScheduleById(id);
        existing.setDayOfWeek(schedule.getDayOfWeek());
        existing.setStartTime(schedule.getStartTime());
        existing.setEndTime(schedule.getEndTime());
        existing.setRoom(schedule.getRoom());
        existing.setLocation(schedule.getLocation());
        existing.setNotes(schedule.getNotes());
        return scheduleRepository.save(existing);
    }
    
    @Override
    public void deleteSchedule(Long id) {
        scheduleRepository.deleteById(id);
        System.out.println("✅ Schedule deleted: " + id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Schedule getScheduleById(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found with id: " + id));
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ScheduleDTO> getSchedulesByClassId(Long classId) {
        List<Schedule> schedules = scheduleRepository.findByClassIdOrderByDayOfWeekAscStartTimeAsc(classId);
        return schedules.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ScheduleDTO> getSchedulesByStudentId(Long studentId) {
        // Get all classes the student is enrolled in
        List<ClassEntity> studentClasses = classRepository.findClassesByStudentId(studentId);
        
        List<ScheduleDTO> allSchedules = new ArrayList<>();
        for (ClassEntity classEntity : studentClasses) {
            List<Schedule> classSchedules = scheduleRepository
                    .findByClassIdOrderByDayOfWeekAscStartTimeAsc(classEntity.getId());
            allSchedules.addAll(classSchedules.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList()));
        }
        
        return allSchedules;
    }
    
    private ScheduleDTO convertToDTO(Schedule schedule) {
        ClassEntity classEntity = classRepository.findById(schedule.getClassId()).orElse(null);
        String className = classEntity != null ? classEntity.getName() : "Unknown";
        
        String courseName = "Unknown";
        if (classEntity != null && classEntity.getCourseId() != null) {
            Course course = courseRepository.findById(classEntity.getCourseId()).orElse(null);
            if (course != null) {
                courseName = course.getName();
            }
        }
        
        return new ScheduleDTO(
                schedule.getId(),
                schedule.getClassId(),
                className,
                courseName,
                schedule.getDayOfWeek(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getRoom(),
                schedule.getLocation(),
                schedule.getNotes()
        );
    }
}
