package com.elearnhub.teacher_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "discussion_likes")
public class DiscussionLike {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "topic_id")
    private Long topicId;
    
    @Column(name = "reply_id")
    private Long replyId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getTopicId() {
        return topicId;
    }
    
    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }
    
    public Long getReplyId() {
        return replyId;
    }
    
    public void setReplyId(Long replyId) {
        this.replyId = replyId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
