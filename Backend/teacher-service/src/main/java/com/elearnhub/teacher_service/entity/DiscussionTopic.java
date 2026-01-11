package com.elearnhub.teacher_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "discussion_topics")
public class DiscussionTopic {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "class_id", nullable = false)
    private Long classId;
    
    @Column(name = "created_by", nullable = false)
    private Long createdBy;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Column(name = "is_pinned")
    private Boolean isPinned = false;
    
    @Column(name = "is_locked")
    private Boolean isLocked = false;
    
    @Column(name = "is_solved")
    private Boolean isSolved = false;
    
    @Column(name = "views_count")
    private Integer viewsCount = 0;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "topicId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DiscussionReply> replies = new ArrayList<>();
    
    @OneToMany(mappedBy = "topicId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DiscussionLike> likes = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
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
    
    public Long getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public Boolean getIsPinned() {
        return isPinned;
    }
    
    public void setIsPinned(Boolean isPinned) {
        this.isPinned = isPinned;
    }
    
    public Boolean getIsLocked() {
        return isLocked;
    }
    
    public void setIsLocked(Boolean isLocked) {
        this.isLocked = isLocked;
    }
    
    public Boolean getIsSolved() {
        return isSolved;
    }
    
    public void setIsSolved(Boolean isSolved) {
        this.isSolved = isSolved;
    }
    
    public Integer getViewsCount() {
        return viewsCount;
    }
    
    public void setViewsCount(Integer viewsCount) {
        this.viewsCount = viewsCount;
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
    
    public List<DiscussionReply> getReplies() {
        return replies;
    }
    
    public void setReplies(List<DiscussionReply> replies) {
        this.replies = replies;
    }
    
    public List<DiscussionLike> getLikes() {
        return likes;
    }
    
    public void setLikes(List<DiscussionLike> likes) {
        this.likes = likes;
    }
}
