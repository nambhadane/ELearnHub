package com.elearnhub.teacher_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.elearnhub.teacher_service.entity.DiscussionTopic;

import java.util.List;

@Repository
public interface DiscussionTopicRepository extends JpaRepository<DiscussionTopic, Long> {
    
    List<DiscussionTopic> findByClassIdOrderByIsPinnedDescCreatedAtDesc(Long classId);
    
    List<DiscussionTopic> findByCreatedByOrderByCreatedAtDesc(Long userId);
    
    @Modifying
    @Query("UPDATE DiscussionTopic t SET t.viewsCount = t.viewsCount + 1 WHERE t.id = :topicId")
    void incrementViewCount(@Param("topicId") Long topicId);
    
    @Query("SELECT COUNT(t) FROM DiscussionTopic t WHERE t.classId = :classId")
    Long countByClassId(@Param("classId") Long classId);
}
