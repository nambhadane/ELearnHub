package com.elearnhub.teacher_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.elearnhub.teacher_service.dto.DiscussionReplyDTO;
import com.elearnhub.teacher_service.dto.DiscussionTopicDTO;
import com.elearnhub.teacher_service.entity.DiscussionLike;
import com.elearnhub.teacher_service.entity.DiscussionReply;
import com.elearnhub.teacher_service.entity.DiscussionTopic;
import com.elearnhub.teacher_service.entity.User;
import com.elearnhub.teacher_service.repository.DiscussionLikeRepository;
import com.elearnhub.teacher_service.repository.DiscussionReplyRepository;
import com.elearnhub.teacher_service.repository.DiscussionTopicRepository;
import com.elearnhub.teacher_service.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DiscussionServiceImpl implements DiscussionService {
    
    @Autowired
    private DiscussionTopicRepository topicRepository;
    
    @Autowired
    private DiscussionReplyRepository replyRepository;
    
    @Autowired
    private DiscussionLikeRepository likeRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public DiscussionTopicDTO createTopic(DiscussionTopicDTO topicDTO) {
        DiscussionTopic topic = new DiscussionTopic();
        topic.setClassId(topicDTO.getClassId());
        topic.setCreatedBy(topicDTO.getCreatedBy());
        topic.setTitle(topicDTO.getTitle());
        topic.setContent(topicDTO.getContent());
        topic.setIsPinned(topicDTO.getIsPinned() != null ? topicDTO.getIsPinned() : false);
        topic.setIsLocked(false);
        topic.setIsSolved(false);
        topic.setViewsCount(0);
        
        topic = topicRepository.save(topic);
        return convertToTopicDTO(topic, topicDTO.getCreatedBy());
    }
    
    @Override
    public DiscussionTopicDTO getTopicById(Long topicId, Long currentUserId) {
        DiscussionTopic topic = topicRepository.findById(topicId)
            .orElseThrow(() -> new RuntimeException("Topic not found"));
        
        // Increment view count
        topicRepository.incrementViewCount(topicId);
        
        return convertToTopicDTO(topic, currentUserId);
    }
    
    @Override
    public List<DiscussionTopicDTO> getTopicsByClass(Long classId, Long currentUserId) {
        List<DiscussionTopic> topics = topicRepository.findByClassIdOrderByIsPinnedDescCreatedAtDesc(classId);
        return topics.stream()
            .map(topic -> convertToTopicDTO(topic, currentUserId))
            .collect(Collectors.toList());
    }
    
    @Override
    public DiscussionTopicDTO updateTopic(Long topicId, DiscussionTopicDTO topicDTO) {
        DiscussionTopic topic = topicRepository.findById(topicId)
            .orElseThrow(() -> new RuntimeException("Topic not found"));
        
        topic.setTitle(topicDTO.getTitle());
        topic.setContent(topicDTO.getContent());
        
        topic = topicRepository.save(topic);
        return convertToTopicDTO(topic, topicDTO.getCreatedBy());
    }
    
    @Override
    public void deleteTopic(Long topicId) {
        topicRepository.deleteById(topicId);
    }
    
    @Override
    public DiscussionTopicDTO pinTopic(Long topicId, Boolean isPinned) {
        DiscussionTopic topic = topicRepository.findById(topicId)
            .orElseThrow(() -> new RuntimeException("Topic not found"));
        
        topic.setIsPinned(isPinned);
        topic = topicRepository.save(topic);
        
        return convertToTopicDTO(topic, topic.getCreatedBy());
    }
    
    @Override
    public DiscussionTopicDTO lockTopic(Long topicId, Boolean isLocked) {
        DiscussionTopic topic = topicRepository.findById(topicId)
            .orElseThrow(() -> new RuntimeException("Topic not found"));
        
        topic.setIsLocked(isLocked);
        topic = topicRepository.save(topic);
        
        return convertToTopicDTO(topic, topic.getCreatedBy());
    }
    
    @Override
    public DiscussionTopicDTO markAsSolved(Long topicId, Boolean isSolved) {
        DiscussionTopic topic = topicRepository.findById(topicId)
            .orElseThrow(() -> new RuntimeException("Topic not found"));
        
        topic.setIsSolved(isSolved);
        topic = topicRepository.save(topic);
        
        return convertToTopicDTO(topic, topic.getCreatedBy());
    }
    
    @Override
    public DiscussionReplyDTO createReply(DiscussionReplyDTO replyDTO) {
        DiscussionReply reply = new DiscussionReply();
        reply.setTopicId(replyDTO.getTopicId());
        reply.setUserId(replyDTO.getUserId());
        reply.setContent(replyDTO.getContent());
        reply.setIsSolution(false);
        
        reply = replyRepository.save(reply);
        return convertToReplyDTO(reply, replyDTO.getUserId());
    }
    
    @Override
    public List<DiscussionReplyDTO> getRepliesByTopic(Long topicId, Long currentUserId) {
        List<DiscussionReply> replies = replyRepository.findByTopicIdOrderByCreatedAtAsc(topicId);
        return replies.stream()
            .map(reply -> convertToReplyDTO(reply, currentUserId))
            .collect(Collectors.toList());
    }
    
    @Override
    public DiscussionReplyDTO updateReply(Long replyId, DiscussionReplyDTO replyDTO) {
        DiscussionReply reply = replyRepository.findById(replyId)
            .orElseThrow(() -> new RuntimeException("Reply not found"));
        
        reply.setContent(replyDTO.getContent());
        reply = replyRepository.save(reply);
        
        return convertToReplyDTO(reply, replyDTO.getUserId());
    }
    
    @Override
    public void deleteReply(Long replyId) {
        replyRepository.deleteById(replyId);
    }
    
    @Override
    public DiscussionReplyDTO markAsSolution(Long replyId, Boolean isSolution) {
        DiscussionReply reply = replyRepository.findById(replyId)
            .orElseThrow(() -> new RuntimeException("Reply not found"));
        
        reply.setIsSolution(isSolution);
        reply = replyRepository.save(reply);
        
        // If marking as solution, mark topic as solved
        if (isSolution) {
            DiscussionTopic topic = topicRepository.findById(reply.getTopicId())
                .orElseThrow(() -> new RuntimeException("Topic not found"));
            topic.setIsSolved(true);
            topicRepository.save(topic);
        }
        
        return convertToReplyDTO(reply, reply.getUserId());
    }
    
    @Override
    public void toggleTopicLike(Long topicId, Long userId) {
        var existingLike = likeRepository.findByTopicIdAndUserId(topicId, userId);
        
        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
        } else {
            DiscussionLike like = new DiscussionLike();
            like.setTopicId(topicId);
            like.setUserId(userId);
            likeRepository.save(like);
        }
    }
    
    @Override
    public void toggleReplyLike(Long replyId, Long userId) {
        var existingLike = likeRepository.findByReplyIdAndUserId(replyId, userId);
        
        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
        } else {
            DiscussionLike like = new DiscussionLike();
            like.setReplyId(replyId);
            like.setUserId(userId);
            likeRepository.save(like);
        }
    }
    
    // Helper methods
    private DiscussionTopicDTO convertToTopicDTO(DiscussionTopic topic, Long currentUserId) {
        DiscussionTopicDTO dto = new DiscussionTopicDTO();
        dto.setId(topic.getId());
        dto.setClassId(topic.getClassId());
        dto.setCreatedBy(topic.getCreatedBy());
        dto.setTitle(topic.getTitle());
        dto.setContent(topic.getContent());
        dto.setIsPinned(topic.getIsPinned());
        dto.setIsLocked(topic.getIsLocked());
        dto.setIsSolved(topic.getIsSolved());
        dto.setViewsCount(topic.getViewsCount());
        dto.setCreatedAt(topic.getCreatedAt());
        dto.setUpdatedAt(topic.getUpdatedAt());
        
        // Get creator info
        User creator = userRepository.findById(topic.getCreatedBy()).orElse(null);
        if (creator != null) {
            dto.setCreatedByName(creator.getName());
            dto.setCreatedByRole(creator.getRole());
        }
        
        // Get counts
        Long repliesCount = replyRepository.countByTopicId(topic.getId());
        dto.setRepliesCount(repliesCount.intValue());
        
        Long likesCount = likeRepository.countByTopicId(topic.getId());
        dto.setLikesCount(likesCount.intValue());
        
        // Check if current user liked
        boolean isLiked = likeRepository.findByTopicIdAndUserId(topic.getId(), currentUserId).isPresent();
        dto.setIsLikedByCurrentUser(isLiked);
        
        return dto;
    }
    
    private DiscussionReplyDTO convertToReplyDTO(DiscussionReply reply, Long currentUserId) {
        DiscussionReplyDTO dto = new DiscussionReplyDTO();
        dto.setId(reply.getId());
        dto.setTopicId(reply.getTopicId());
        dto.setUserId(reply.getUserId());
        dto.setContent(reply.getContent());
        dto.setIsSolution(reply.getIsSolution());
        dto.setCreatedAt(reply.getCreatedAt());
        dto.setUpdatedAt(reply.getUpdatedAt());
        
        // Get user info
        User user = userRepository.findById(reply.getUserId()).orElse(null);
        if (user != null) {
            dto.setUserName(user.getName());
            dto.setUserRole(user.getRole());
        }
        
        // Get likes count
        Long likesCount = likeRepository.countByReplyId(reply.getId());
        dto.setLikesCount(likesCount.intValue());
        
        // Check if current user liked
        boolean isLiked = likeRepository.findByReplyIdAndUserId(reply.getId(), currentUserId).isPresent();
        dto.setIsLikedByCurrentUser(isLiked);
        
        return dto;
    }
}
