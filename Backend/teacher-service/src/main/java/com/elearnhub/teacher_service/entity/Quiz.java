package com.elearnhub.teacher_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quizzes")
public class Quiz {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long courseId; // Changed from classId to courseId for consistency
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private LocalDateTime dueDate; // Changed from endTime to dueDate for consistency
    
    @Column(nullable = false)
    private LocalDateTime startTime;
    
    @Column(nullable = false)
    private Integer timeLimit; // Changed from duration to timeLimit for consistency
    
    @Column(nullable = false)
    private Integer maxGrade; // Changed from totalMarks to maxGrade for consistency
    
    @Column(nullable = false)
    private Integer passingMarks;
    
    @Column(nullable = false)
    private Boolean shuffleQuestions = false; // Changed from randomizeQuestions
    
    @Column(nullable = false)
    private Boolean showResults = true; // Changed from showResultsImmediately
    
    @Column(nullable = false)
    private Boolean allowRetakes = false; // New field for retakes
    
    @Column(nullable = false)
    private Integer maxAttempts = 1;
    
    @Column(nullable = false)
    private String status = "DRAFT"; // DRAFT, PUBLISHED, CLOSED
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions = new ArrayList<>();
    
    // Constructors
    public Quiz() {
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getCourseId() {
        return courseId;
    }
    
    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }
    
    // Backward compatibility - some code might still use classId
    public Long getClassId() {
        return courseId;
    }
    
    public void setClassId(Long classId) {
        this.courseId = classId;
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
    
    public LocalDateTime getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    // Backward compatibility - some code might still use endTime
    public LocalDateTime getEndTime() {
        return dueDate;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.dueDate = endTime;
    }
    
    public Integer getTimeLimit() {
        return timeLimit;
    }
    
    public void setTimeLimit(Integer timeLimit) {
        this.timeLimit = timeLimit;
    }
    
    // Backward compatibility - some code might still use duration
    public Integer getDuration() {
        return timeLimit;
    }
    
    public void setDuration(Integer duration) {
        this.timeLimit = duration;
    }
    
    public Integer getMaxGrade() {
        return maxGrade;
    }
    
    public void setMaxGrade(Integer maxGrade) {
        this.maxGrade = maxGrade;
    }
    
    // Backward compatibility - some code might still use totalMarks
    public Integer getTotalMarks() {
        return maxGrade;
    }
    
    public void setTotalMarks(Integer totalMarks) {
        this.maxGrade = totalMarks;
    }
    
    public Integer getPassingMarks() {
        return passingMarks;
    }
    
    public void setPassingMarks(Integer passingMarks) {
        this.passingMarks = passingMarks;
    }
    
    public Boolean getShuffleQuestions() {
        return shuffleQuestions;
    }
    
    public void setShuffleQuestions(Boolean shuffleQuestions) {
        this.shuffleQuestions = shuffleQuestions;
    }
    
    // Backward compatibility
    public Boolean getRandomizeQuestions() {
        return shuffleQuestions;
    }
    
    public void setRandomizeQuestions(Boolean randomizeQuestions) {
        this.shuffleQuestions = randomizeQuestions;
    }
    
    public Boolean getShowResults() {
        return showResults;
    }
    
    public void setShowResults(Boolean showResults) {
        this.showResults = showResults;
    }
    
    // Backward compatibility
    public Boolean getShowResultsImmediately() {
        return showResults;
    }
    
    public void setShowResultsImmediately(Boolean showResultsImmediately) {
        this.showResults = showResultsImmediately;
    }
    
    public Boolean getAllowRetakes() {
        return allowRetakes;
    }
    
    public void setAllowRetakes(Boolean allowRetakes) {
        this.allowRetakes = allowRetakes;
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
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public List<Question> getQuestions() {
        return questions;
    }
    
    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
