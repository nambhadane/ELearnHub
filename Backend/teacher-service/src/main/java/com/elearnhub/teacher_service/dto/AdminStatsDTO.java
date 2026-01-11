package com.elearnhub.teacher_service.dto;

public class AdminStatsDTO {
    private Long totalUsers;
    private Long totalStudents;
    private Long totalTeachers;
    private Long totalAdmins;
    private Long totalCourses;
    private Long totalClasses;
    private Long activeClasses;
    private Long totalAssignments;
    private Long totalQuizzes;
    private Long totalMaterials;
    private Double averageAttendance;
    private Long activeUsersToday;
    
    // Constructors
    public AdminStatsDTO() {}
    
    // Getters and Setters
    public Long getTotalUsers() {
        return totalUsers;
    }
    
    public void setTotalUsers(Long totalUsers) {
        this.totalUsers = totalUsers;
    }
    
    public Long getTotalStudents() {
        return totalStudents;
    }
    
    public void setTotalStudents(Long totalStudents) {
        this.totalStudents = totalStudents;
    }
    
    public Long getTotalTeachers() {
        return totalTeachers;
    }
    
    public void setTotalTeachers(Long totalTeachers) {
        this.totalTeachers = totalTeachers;
    }
    
    public Long getTotalAdmins() {
        return totalAdmins;
    }
    
    public void setTotalAdmins(Long totalAdmins) {
        this.totalAdmins = totalAdmins;
    }
    
    public Long getTotalCourses() {
        return totalCourses;
    }
    
    public void setTotalCourses(Long totalCourses) {
        this.totalCourses = totalCourses;
    }
    
    public Long getTotalClasses() {
        return totalClasses;
    }
    
    public void setTotalClasses(Long totalClasses) {
        this.totalClasses = totalClasses;
    }
    
    public Long getActiveClasses() {
        return activeClasses;
    }
    
    public void setActiveClasses(Long activeClasses) {
        this.activeClasses = activeClasses;
    }
    
    public Long getTotalAssignments() {
        return totalAssignments;
    }
    
    public void setTotalAssignments(Long totalAssignments) {
        this.totalAssignments = totalAssignments;
    }
    
    public Long getTotalQuizzes() {
        return totalQuizzes;
    }
    
    public void setTotalQuizzes(Long totalQuizzes) {
        this.totalQuizzes = totalQuizzes;
    }
    
    public Long getTotalMaterials() {
        return totalMaterials;
    }
    
    public void setTotalMaterials(Long totalMaterials) {
        this.totalMaterials = totalMaterials;
    }
    
    public Double getAverageAttendance() {
        return averageAttendance;
    }
    
    public void setAverageAttendance(Double averageAttendance) {
        this.averageAttendance = averageAttendance;
    }
    
    public Long getActiveUsersToday() {
        return activeUsersToday;
    }
    
    public void setActiveUsersToday(Long activeUsersToday) {
        this.activeUsersToday = activeUsersToday;
    }
}
