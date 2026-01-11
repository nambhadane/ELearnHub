package com.elearnhub.teacher_service.dto;

public class CourseDTO {
    private Long id;
    private String name;
    private String description;
    private Long teacherId;

    public CourseDTO() {}

    public CourseDTO(Long id, String name, String description, Long teacherId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.teacherId = teacherId;
    }

    // Getters and setters
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }

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
    
}