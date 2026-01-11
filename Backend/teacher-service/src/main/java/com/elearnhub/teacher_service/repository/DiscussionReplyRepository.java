package com.elearnhub.teacher_service.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.elearnhub.teacher_service.entity.DiscussionReply;

import java.util.List;

@Repository
public interface DiscussionReplyRepository extends JpaRepository<DiscussionReply, Long> {
    
    List<DiscussionReply> findByTopicIdOrderByCreatedAtAsc(Long topicId);
    
    List<DiscussionReply> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    @Query("SELECT COUNT(r) FROM DiscussionReply r WHERE r.topicId = :topicId")
    Long countByTopicId(@Param("topicId") Long topicId);
    
    void deleteByTopicId(Long topicId);
}
