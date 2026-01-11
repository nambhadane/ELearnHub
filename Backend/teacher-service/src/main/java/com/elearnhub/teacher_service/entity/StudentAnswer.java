package com.elearnhub.teacher_service.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "student_answers")
public class StudentAnswer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private QuizAttempt attempt;
    
    @Column(nullable = false)
    private Long questionId;
    
    @Column(columnDefinition = "TEXT")
    private String answerText; // For SHORT_ANSWER
    
    @Column
    private Long selectedOptionId; // For MULTIPLE_CHOICE
    
    @Column
    private Boolean isCorrect;
    
    @Column
    private Integer marksAwarded;
    
    // Constructors
    public StudentAnswer() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public QuizAttempt getAttempt() {
        return attempt;
    }
    
    public void setAttempt(QuizAttempt attempt) {
        this.attempt = attempt;
    }
    
    public Long getQuestionId() {
        return questionId;
    }
    
    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }
    
    public String getAnswerText() {
        return answerText;
    }
    
    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }
    
    public Long getSelectedOptionId() {
        return selectedOptionId;
    }
    
    public void setSelectedOptionId(Long selectedOptionId) {
        this.selectedOptionId = selectedOptionId;
    }
    
    public Boolean getIsCorrect() {
        return isCorrect;
    }
    
    public void setIsCorrect(Boolean isCorrect) {
        this.isCorrect = isCorrect;
    }
    
    public Integer getMarksAwarded() {
        return marksAwarded;
    }
    
    public void setMarksAwarded(Integer marksAwarded) {
        this.marksAwarded = marksAwarded;
    }

	public Object getScore() {
		// TODO Auto-generated method stub
		return null;
	}
}
