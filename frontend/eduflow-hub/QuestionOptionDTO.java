package com.elearnhub.teacher_service.dto;

public class QuestionOptionDTO {
    private Long id;
    private String optionText;
    private Boolean isCorrect; // Only for teacher view
    private Integer orderIndex;
    
    // Constructors
    public QuestionOptionDTO() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getOptionText() {
        return optionText;
    }
    
    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }
    
    public Boolean getIsCorrect() {
        return isCorrect;
    }
    
    public void setIsCorrect(Boolean isCorrect) {
        this.isCorrect = isCorrect;
    }
    
    public Integer getOrderIndex() {
        return orderIndex;
    }
    
    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }
}
