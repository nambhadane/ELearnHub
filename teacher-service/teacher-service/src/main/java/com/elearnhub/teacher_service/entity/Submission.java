package com.elearnhub.teacher_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "submission")
public class Submission {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;
    
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;  // User with STUDENT role
    
    private String content;
    private String filePath;
    private LocalDateTime submittedAt;
    private Double grade;
    private String feedback;
    
    

    // Constructors
    public Submission() {
    }

    

    public Submission(Long id, Assignment assignment, User student, String content, String filePath,
			LocalDateTime submittedAt, Double grade, String feedback) {
		super();
		this.id = id;
		this.assignment = assignment;
		this.student = student;
		this.content = content;
		this.filePath = filePath;
		this.submittedAt = submittedAt;
		this.grade = grade;
		this.feedback = feedback;
	}

	// Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

  

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }



	public Assignment getAssignment() {
		return assignment;
	}



	public void setAssignment(Assignment assignment) {
		this.assignment = assignment;
	}



	public User getStudent() {
		return student;
	}



	public void setStudent(User student) {
		this.student = student;
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
    
    
}

