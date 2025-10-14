package com.elearnhub.teacher_service.dto;

public class LessonDTO {
    private Long id;
    private String title;
    private String filePath;
    private Long classId;

    public LessonDTO() {}

    public LessonDTO(Long id, String title, String filePath, Long classId) {
        this.id = id;
        this.title = title;
        this.filePath = filePath;
        this.classId = classId;
    }

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

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public Long getClassId() {
		return classId;
	}

	public void setClassId(Long classId) {
		this.classId = classId;
	}

    // Getters and setters
    
    
    
}