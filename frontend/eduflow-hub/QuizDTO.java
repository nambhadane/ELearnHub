package com.elearnhub.teacher_service.dto;

import java.time.LocalDateTime;
import java.util.List;

public class QuizDTO {
    private Long id;
    private Long classId;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer duration;
    private Integer totalMarks;
    private Integer passingMarks;
    private Boolean randomizeQuestions;
    private Boolean showResultsImmediately;
    private Integer maxAttempts;
    private String status;
    private Integer questionCount;
    private LocalDateTime createdAt;
    private List<QuestionDTO> questions;
    
    // For student view
    private Integer attemptsUsed;
    private Integer bestScore;
    private Boolean canAttempt;
    
    // Constructors
    public QuizDTO() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getClassId() {
        return classId;
    }
    
    public void setClassId(Long classId) {
        this.classId = classId;
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
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public Integer getDuration() {
        return duration;
    }
    
    public void setDuration(Integer duration) {
        this.duration = duration;
    }
    
    public Integer getTotalMarks() {
        return totalMarks;
    }
    
    public void setTotalMarks(Integer totalMarks) {
        this.totalMarks = totalMarks;
    }
    
    public Integer getPassingMarks() {
        return passingMarks;
    }
    
    public void setPassingMarks(Integer passingMarks) {
        this.passingMarks = passingMarks;
    }
    
    public Boolean getRandomizeQuestions() {
        return randomizeQuestions;
    }
    
    public void setRandomizeQuestions(Boolean randomizeQuestions) {
        this.randomizeQuestions = randomizeQuestions;
    }
    
    public Boolean getShowResultsImmediately() {
        return showResultsImmediately;
    }
    
    public void setShowResultsImmediately(Boolean showResultsImmediately) {
        this.showResultsImmediately = showResultsImmediately;
    }
    
    public Integer getMaxAttempts() {
        return maxAttempts;
    }
    
    public void setMaxAttempts(Integer maxAttempts) {
        this.maxAttempts = maxAttempts;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Integer getQuestionCount() {
        return questionCount;
    }
    
    public void setQuestionCount(Integer questionCount) {
        this.questionCount = questionCount;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public List<QuestionDTO> getQuestions() {
        return questions;
    }
    
    public void setQuestions(List<QuestionDTO> questions) {
        this.questions = questions;
    }
    
    public Integer getAttemptsUsed() {
        return attemptsUsed;
    }
    
    public void setAttemptsUsed(Integer attemptsUsed) {
        this.attemptsUsed = attemptsUsed;
    }
    
    public Integer getBestScore() {
        return bestScore;
    }
    
    public void setBestScore(Integer bestScore) {
        this.bestScore = bestScore;
    }
    
    public Boolean getCanAttempt() {
        return canAttempt;
    }
    
    public void setCanAttempt(Boolean canAttempt) {
        this.canAttempt = canAttempt;
    }
}
