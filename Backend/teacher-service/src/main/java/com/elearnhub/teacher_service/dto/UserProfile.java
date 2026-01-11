package com.elearnhub.teacher_service.dto;

import java.util.List;

public class UserProfile {
    private Long id;
    private String username;
    private String name;
    private String email;
    private String role;
    
    // Common fields
    private String phone;
    private String location;
    private String joinDate;
    
    // Student specific
    private String studentId;
    private String major;
    private String year;
    private String expectedGraduation;
    private Double gpa;
    private Integer creditsCompleted;
    private Integer totalCredits;
    private Double attendanceRate;
    
    // Teacher specific
    private String department;
    private String specialization;
    private Integer yearsOfExperience;
    private Integer totalClasses;
    private Integer totalStudents;
    
    // Admin specific
    private String adminLevel;
    private List<String> permissions;
	public UserProfile(Long id, String username, String name, String email, String role, String phone, String location,
			String joinDate, String studentId, String major, String year, String expectedGraduation, Double gpa,
			Integer creditsCompleted, Integer totalCredits, Double attendanceRate, String department,
			String specialization, Integer yearsOfExperience, Integer totalClasses, Integer totalStudents,
			String adminLevel, List<String> permissions) {
		super();
		this.id = id;
		this.username = username;
		this.name = name;
		this.email = email;
		this.role = role;
		this.phone = phone;
		this.location = location;
		this.joinDate = joinDate;
		this.studentId = studentId;
		this.major = major;
		this.year = year;
		this.expectedGraduation = expectedGraduation;
		this.gpa = gpa;
		this.creditsCompleted = creditsCompleted;
		this.totalCredits = totalCredits;
		this.attendanceRate = attendanceRate;
		this.department = department;
		this.specialization = specialization;
		this.yearsOfExperience = yearsOfExperience;
		this.totalClasses = totalClasses;
		this.totalStudents = totalStudents;
		this.adminLevel = adminLevel;
		this.permissions = permissions;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getJoinDate() {
		return joinDate;
	}
	public void setJoinDate(String joinDate) {
		this.joinDate = joinDate;
	}
	public String getStudentId() {
		return studentId;
	}
	public void setStudentId(String studentId) {
		this.studentId = studentId;
	}
	public String getMajor() {
		return major;
	}
	public void setMajor(String major) {
		this.major = major;
	}
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	public String getExpectedGraduation() {
		return expectedGraduation;
	}
	public void setExpectedGraduation(String expectedGraduation) {
		this.expectedGraduation = expectedGraduation;
	}
	public Double getGpa() {
		return gpa;
	}
	public void setGpa(Double gpa) {
		this.gpa = gpa;
	}
	public Integer getCreditsCompleted() {
		return creditsCompleted;
	}
	public void setCreditsCompleted(Integer creditsCompleted) {
		this.creditsCompleted = creditsCompleted;
	}
	public Integer getTotalCredits() {
		return totalCredits;
	}
	public void setTotalCredits(Integer totalCredits) {
		this.totalCredits = totalCredits;
	}
	public Double getAttendanceRate() {
		return attendanceRate;
	}
	public void setAttendanceRate(Double attendanceRate) {
		this.attendanceRate = attendanceRate;
	}
	public String getDepartment() {
		return department;
	}
	public void setDepartment(String department) {
		this.department = department;
	}
	public String getSpecialization() {
		return specialization;
	}
	public void setSpecialization(String specialization) {
		this.specialization = specialization;
	}
	public Integer getYearsOfExperience() {
		return yearsOfExperience;
	}
	public void setYearsOfExperience(Integer yearsOfExperience) {
		this.yearsOfExperience = yearsOfExperience;
	}
	public Integer getTotalClasses() {
		return totalClasses;
	}
	public void setTotalClasses(Integer totalClasses) {
		this.totalClasses = totalClasses;
	}
	public Integer getTotalStudents() {
		return totalStudents;
	}
	public void setTotalStudents(Integer totalStudents) {
		this.totalStudents = totalStudents;
	}
	public String getAdminLevel() {
		return adminLevel;
	}
	public void setAdminLevel(String adminLevel) {
		this.adminLevel = adminLevel;
	}
	public List<String> getPermissions() {
		return permissions;
	}
	public void setPermissions(List<String> permissions) {
		this.permissions = permissions;
	}
	public UserProfile() {
		super();
		// TODO Auto-generated constructor stub
	}
    
    // Getters and setters...
    
    
    
}
