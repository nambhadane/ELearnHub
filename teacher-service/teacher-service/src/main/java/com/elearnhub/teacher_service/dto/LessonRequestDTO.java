package com.elearnhub.teacher_service.dto;

import org.springframework.web.multipart.MultipartFile;

public class LessonRequestDTO {
    private Long courseId; // Assuming courseId should be classId based on your endpoint
    private String title;
    private String content;
    private MultipartFile file;

    // Getters, setters, and no-args constructor
    public LessonRequestDTO() {}

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public MultipartFile getFile() { return file; }
    public void setFile(MultipartFile file) { this.file = file; }
}