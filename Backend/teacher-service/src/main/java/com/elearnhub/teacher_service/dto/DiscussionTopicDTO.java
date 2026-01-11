package com.elearnhub.teacher_service.dto;


import java.time.LocalDateTime;
import java.util.List;

public class DiscussionTopicDTO {
    private Long id;
    private Long classId;
    private Long createdBy;
    private String createdByName;
    private String createdByRole;
    private String title;
    private String content;
    private Boolean isPinned;
    private Boolean isLocked;
    private Boolean isSolved;
    private Integer viewsCount;
    private Integer repliesCount;
    private Integer likesCount;
    private Boolean isLikedByCurrentUser;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<DiscussionReplyDTO> replies;
    
    // Constructors
    public DiscussionTopicDTO() {}
    
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
    
    public String getCreatedByName() {
        return createdByName;
    }
    
    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }
    
    public String getCreatedByRole() {
        return createdByRole;
    }
    
    public void setCreatedByRole(String createdByRole) {
        this.createdByRole = createdByRole;
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
    
    public Integer getRepliesCount() {
        return repliesCount;
    }
    
    public void setRepliesCount(Integer repliesCount) {
        this.repliesCount = repliesCount;
    }
    
    public Integer getLikesCount() {
        return likesCount;
    }
    
    public void setLikesCount(Integer likesCount) {
        this.likesCount = likesCount;
    }
    
    public Boolean getIsLikedByCurrentUser() {
        return isLikedByCurrentUser;
    }
    
    public void setIsLikedByCurrentUser(Boolean isLikedByCurrentUser) {
        this.isLikedByCurrentUser = isLikedByCurrentUser;
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
    
    public List<DiscussionReplyDTO> getReplies() {
        return replies;
    }
    
    public void setReplies(List<DiscussionReplyDTO> replies) {
        this.replies = replies;
    }
}
