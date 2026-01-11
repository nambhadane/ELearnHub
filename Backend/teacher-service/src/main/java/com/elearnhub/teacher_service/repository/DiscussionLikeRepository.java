package com.elearnhub.teacher_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.elearnhub.teacher_service.entity.DiscussionLike;

import java.util.Optional;

@Repository
public interface DiscussionLikeRepository extends JpaRepository<DiscussionLike, Long> {
    
    Optional<DiscussionLike> findByTopicIdAndUserId(Long topicId, Long userId);
    
    Optional<DiscussionLike> findByReplyIdAndUserId(Long replyId, Long userId);
    
    @Query("SELECT COUNT(l) FROM DiscussionLike l WHERE l.topicId = :topicId")
    Long countByTopicId(@Param("topicId") Long topicId);
    
    @Query("SELECT COUNT(l) FROM DiscussionLike l WHERE l.replyId = :replyId")
    Long countByReplyId(@Param("replyId") Long replyId);
    
    void deleteByTopicId(Long topicId);
    
    void deleteByReplyId(Long replyId);
}
