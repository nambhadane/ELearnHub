//package com.elearnhub.teacher_service.entity;
//
//import jakarta.persistence.*;
//import jakarta.validation.constraints.Max;
//import jakarta.validation.constraints.Min;
//import jakarta.validation.constraints.NotNull;
//import jakarta.validation.constraints.Size;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//@Entity
//@Data
//@NoArgsConstructor
//public class Grade {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @NotNull(message = "Submission ID cannot be null")
//    private Long submissionId;
//
//    @NotNull(message = "Score cannot be null")
//    @Min(value = 0, message = "Score must be at least 0")
//    @Max(value = 100, message = "Score must not exceed 100")
//    private Double score;
//
//    @Size(max = 500, message = "Feedback must not exceed 500 characters")
//    private String feedback;
//	public Grade() {
//		super();
//		// TODO Auto-generated constructor stub
//	}
//	public Grade(Long id, Long submissionId, Double score, String feedback) {
//		super();
//		this.id = id;
//		this.submissionId = submissionId;
//		this.score = score;
//		this.feedback = feedback;
//	}
//	public Long getId() {
//		return id;
//	}
//	public void setId(Long id) {
//		this.id = id;
//	}
//	public Long getSubmissionId() {
//		return submissionId;
//	}
//	public void setSubmissionId(Long submissionId) {
//		this.submissionId = submissionId;
//	}
//	public Double getScore() {
//		return score;
//	}
//	public void setScore(Double score) {
//		this.score = score;
//	}
//	public String getFeedback() {
//		return feedback;
//	}
//	public void setFeedback(String feedback) {
//		this.feedback = feedback;
//	}
//    
//    
//}


package com.elearnhub.teacher_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Submission ID cannot be null")
    private Long submissionId;

    @NotNull(message = "Score cannot be null")
    @Min(value = 0, message = "Score must be at least 0")
    @Max(value = 100, message = "Score must not exceed 100")
    private Double score;

    @Size(max = 500, message = "Feedback must not exceed 500 characters")
    private String feedback;

    // No-Args Constructor
    public Grade() {}

    // All-Args Constructor
    public Grade(Long id, Long submissionId, Double score, String feedback) {
        this.id = id;
        this.submissionId = submissionId;
        this.score = score;
        this.feedback = feedback;
    }

    // Getters & Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(Long submissionId) {
        this.submissionId = submissionId;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}
