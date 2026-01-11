package com.elearnhub.teacher_service.dto;

import java.time.LocalDateTime;

public class MaterialDTO {
    private Long id;
    private String title;
    private String description;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private Long classId;
    private String uploadedByName;
    private LocalDateTime uploadedAt;
    
    // Constructors
    public MaterialDTO() {}
    
    public MaterialDTO(Long id, String title, String description, String fileName, 
                      String fileType, Long fileSize, Long classId, 
                      String uploadedByName, LocalDateTime uploadedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.classId = classId;
        this.uploadedByName = uploadedByName;
        this.uploadedAt = uploadedAt;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public String getFileType() {
        return fileType;
    }
    
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    
    public Long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    
    public Long getClassId() {
        return classId;
    }
    
    public void setClassId(Long classId) {
        this.classId = classId;
    }
    
    public String getUploadedByName() {
        return uploadedByName;
    }
    
    public void setUploadedByName(String uploadedByName) {
        this.uploadedByName = uploadedByName;
    }
    
    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }
    
    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
