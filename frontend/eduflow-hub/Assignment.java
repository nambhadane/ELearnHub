package com.elearnhub.teacher_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "assignment")
public class Assignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Title cannot be null")
    @Size(min = 2, max = 100, message = "Title must be between 2 and 100 characters")
    @Column(nullable = false, length = 100)
    private String title;

    @NotNull(message = "Description cannot be null")
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Column(nullable = false, length = 1000)
    private String description;

    @NotNull(message = "Due date cannot be null")
    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;

    @NotNull(message = "Max grade cannot be null")
    @Column(name = "max_grade", nullable = false)
    private Double maxGrade;

    @NotNull(message = "Course ID cannot be null")
    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "class_id")
    private Long classId;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "allow_late_submission", nullable = false)
    private Boolean allowLateSubmission = false;

    @Column(name = "late_penalty")
    private Double latePenalty;

    @Column(name = "additional_instructions", length = 1000)
    private String additionalInstructions;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "published";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (allowLateSubmission == null) {
            allowLateSubmission = false;
        }
        if (status == null || status.isEmpty()) {
            status = "published";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructors
    public Assignment() {
        this.allowLateSubmission = false;
        this.status = "published";
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Double getMaxGrade() {
        return maxGrade;
    }

    public void setMaxGrade(Double maxGrade) {
        this.maxGrade = maxGrade;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Boolean getAllowLateSubmission() {
        return allowLateSubmission;
    }

    public void setAllowLateSubmission(Boolean allowLateSubmission) {
        this.allowLateSubmission = allowLateSubmission;
    }

    public Double getLatePenalty() {
        return latePenalty;
    }

    public void setLatePenalty(Double latePenalty) {
        this.latePenalty = latePenalty;
    }

    public String getAdditionalInstructions() {
        return additionalInstructions;
    }

    public void setAdditionalInstructions(String additionalInstructions) {
        this.additionalInstructions = additionalInstructions;
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
}
