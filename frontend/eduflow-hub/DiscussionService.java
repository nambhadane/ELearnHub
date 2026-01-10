package com.elearnhub.teacher_service.service;

import com.elearnhub.teacher_service.dto.DiscussionTopicDTO;
import com.elearnhub.teacher_service.dto.DiscussionReplyDTO;

import java.util.List;

public interface DiscussionService {
    
    // Topic operations
    DiscussionTopicDTO createTopic(DiscussionTopicDTO topicDTO);
    
    DiscussionTopicDTO getTopicById(Long topicId, Long currentUserId);
    
    List<DiscussionTopicDTO> getTopicsByClass(Long classId, Long currentUserId);
    
    DiscussionTopicDTO updateTopic(Long topicId, DiscussionTopicDTO topicDTO);
    
    void deleteTopic(Long topicId);
    
    DiscussionTopicDTO pinTopic(Long topicId, Boolean isPinned);
    
    DiscussionTopicDTO lockTopic(Long topicId, Boolean isLocked);
    
    DiscussionTopicDTO markAsSolved(Long topicId, Boolean isSolved);
    
    // Reply operations
    DiscussionReplyDTO createReply(DiscussionReplyDTO replyDTO);
    
    List<DiscussionReplyDTO> getRepliesByTopic(Long topicId, Long currentUserId);
    
    DiscussionReplyDTO updateReply(Long replyId, DiscussionReplyDTO replyDTO);
    
    void deleteReply(Long replyId);
    
    DiscussionReplyDTO markAsSolution(Long replyId, Boolean isSolution);
    
    // Like operations
    void toggleTopicLike(Long topicId, Long userId);
    
    void toggleReplyLike(Long replyId, Long userId);
}
