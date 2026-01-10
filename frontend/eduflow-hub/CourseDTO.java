package com.elearnhub.teacher_service.dto;

import java.time.LocalDateTime;

public class CourseDTO {
    private Long id;
    private String name;
    private String description;
    private Long teacherId;
    private String teacherName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int students; // Number of enrolled students

    // Default constructor
    public CourseDTO() {}

    // Constructor with all fields
    public CourseDTO(Long id, String name, String description, Long teacherId, String teacherName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
    }

    // Constructor without teacher info
    public CourseDTO(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
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

    public int getStudents() {
        return students;
    }

    public void setStudents(int students) {
        this.students = students;
    }
}