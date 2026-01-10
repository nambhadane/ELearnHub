package com.elearnhub.teacher_service.dto;

public class ClassDTO {
    private Long id;
    private String name;
    private Long teacherId;
    private String teacherName;
    private Long courseId;
    private String courseName;
    private Integer studentCount;

    // Default constructor
    public ClassDTO() {}

    // Constructor with all fields
    public ClassDTO(Long id, String name, Long teacherId, Long courseId, Integer studentCount) {
        this.id = id;
        this.name = name;
        this.teacherId = teacherId;
        this.courseId = courseId;
        this.studentCount = studentCount;
    }

    // Simple constructor
    public ClassDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    // Constructor with teacher and course names
    public ClassDTO(Long id, String name, Long teacherId, String teacherName, Long courseId, String courseName, Integer studentCount) {
        this.id = id;
        this.name = name;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.courseId = courseId;
        this.courseName = courseName;
        this.studentCount = studentCount;
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

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public Integer getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(Integer studentCount) {
        this.studentCount = studentCount;
    }
}