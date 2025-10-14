package com.elearnhub.teacher_service.dto;

public class ClassDTO {
    private Long id;
    private String name;
    private Long teacherId;
    private Long courseId;

    public ClassDTO() {}

    public ClassDTO(Long id, String name, Long teacherId, Long courseId) {
        this.id = id;
        this.name = name;
        this.teacherId = teacherId;
        this.courseId = courseId;
    }

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

	public Long getCourseId() {
		return courseId;
	}

	public void setCourseId(Long courseId) {
		this.courseId = courseId;
	}

    // Getters and setters
    
    
}