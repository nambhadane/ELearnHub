package com.elearnhub.teacher_service.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;

public class AttendanceSessionDTO {
    
    private Long id;
    private Long classId;
    private LocalDate sessionDate;
    private LocalTime sessionTime;
    private String title;
    private String description;
    private Long createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<AttendanceRecordDTO> records;
    private Integer totalStudents;
    private Integer presentCount;
    private Integer absentCount;
    private Integer lateCount;
    private Double attendancePercentage;
    
    // Constructors
    public AttendanceSessionDTO() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getClassId() {
        return classId;
    }
    
    public void setClassId(Long classId) {
        this.classId = classId;
    }
    
    public LocalDate getSessionDate() {
        return sessionDate;
    }
    
    public void setSessionDate(LocalDate sessionDate) {
        this.sessionDate = sessionDate;
    }
    
    public LocalTime getSessionTime() {
        return sessionTime;
    }
    
    public void setSessionTime(LocalTime sessionTime) {
        this.sessionTime = sessionTime;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Long getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }
    
    public String getCreatedByName() {
        return createdByName;
    }
    
    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public List<AttendanceRecordDTO> getRecords() {
        return records;
    }
    
    public void setRecords(List<AttendanceRecordDTO> records) {
        this.records = records;
    }
    
    public Integer getTotalStudents() {
        return totalStudents;
    }
    
    public void setTotalStudents(Integer totalStudents) {
        this.totalStudents = totalStudents;
    }
    
    public Integer getPresentCount() {
        return presentCount;
    }
    
    public void setPresentCount(Integer presentCount) {
        this.presentCount = presentCount;
    }
    
    public Integer getAbsentCount() {
        return absentCount;
    }
    
    public void setAbsentCount(Integer absentCount) {
        this.absentCount = absentCount;
    }
    
    public Integer getLateCount() {
        return lateCount;
    }
    
    public void setLateCount(Integer lateCount) {
        this.lateCount = lateCount;
    }
    
    public Double getAttendancePercentage() {
        return attendancePercentage;
    }
    
    public void setAttendancePercentage(Double attendancePercentage) {
        this.attendancePercentage = attendancePercentage;
    }
}
