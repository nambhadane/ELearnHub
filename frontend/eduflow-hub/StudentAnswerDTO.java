package com.elearnhub.teacher_service.dto;

public class StudentAnswerDTO {
    private Long id;
    private Long questionId;
    private String answerText;
    private Long selectedOptionId;
    private Boolean isCorrect;
    private Integer marksAwarded;
    
    // Constructors
    public StudentAnswerDTO() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
}
