package com.elearnhub.teacher_service.dto;

public class AttendanceStatisticsDTO {
    
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private Long classId;
    private String className;
    private Integer totalSessions;
    private Integer presentCount;
    private Integer absentCount;
    private Integer lateCount;
    private Double attendancePercentage;
    
    // Constructors
    public AttendanceStatisticsDTO() {}
    
    // Getters and Setters
    public Long getStudentId() {
        return studentId;
    }
    
    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }
    
    public String getStudentName() {
        return studentName;
    }
    
    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }
    
    public String getStudentEmail() {
        return studentEmail;
    }
    
    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }
    
    public Long getClassId() {
        return classId;
    }
    
    public void setClassId(Long classId) {
        this.classId = classId;
    }
    
    public String getClassName() {
        return className;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }
    
    public Integer getTotalSessions() {
        return totalSessions;
    }
    
    public void setTotalSessions(Integer totalSessions) {
        this.totalSessions = totalSessions;
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
