package com.elearnhub.teacher_service.dto;

public class GradeRequest {
    private Double grade;
    private String feedback;
    
    
	public GradeRequest(Double grade, String feedback) {
		super();
		this.grade = grade;
		this.feedback = feedback;
	}
	public Double getGrade() {
		return grade;
	}
	public void setGrade(Double grade) {
		this.grade = grade;
	}
	public String getFeedback() {
		return feedback;
	}
	public void setFeedback(String feedback) {
		this.feedback = feedback;
	}

    // Getters, setters
    
}
