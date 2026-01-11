package com.elearnhub.teacher_service.Controller;

import com.elearnhub.teacher_service.dto.DiscussionTopicDTO;
import com.elearnhub.teacher_service.dto.DiscussionReplyDTO;
import com.elearnhub.teacher_service.service.DiscussionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/discussions")
@CrossOrigin(origins = {"http://localhost:8081", "http://localhost:5173"}, allowCredentials = "true")
public class DiscussionController {
    
    @Autowired
    private DiscussionService discussionService;
    
    // Topic endpoints
    @PostMapping("/topics")
    public ResponseEntity<DiscussionTopicDTO> createTopic(@RequestBody DiscussionTopicDTO topicDTO) {
        DiscussionTopicDTO created = discussionService.createTopic(topicDTO);
        return ResponseEntity.ok(created);
    }
    
    @GetMapping("/topics/{topicId}")
    public ResponseEntity<DiscussionTopicDTO> getTopicById(
            @PathVariable Long topicId,
            @RequestParam Long currentUserId) {
        DiscussionTopicDTO topic = discussionService.getTopicById(topicId, currentUserId);
        return ResponseEntity.ok(topic);
    }
    
    @GetMapping("/topics/class/{classId}")
    public ResponseEntity<List<DiscussionTopicDTO>> getTopicsByClass(
            @PathVariable Long classId,
            @RequestParam Long currentUserId) {
        List<DiscussionTopicDTO> topics = discussionService.getTopicsByClass(classId, currentUserId);
        return ResponseEntity.ok(topics);
    }
    
    @PutMapping("/topics/{topicId}")
    public ResponseEntity<DiscussionTopicDTO> updateTopic(
            @PathVariable Long topicId,
            @RequestBody DiscussionTopicDTO topicDTO) {
        DiscussionTopicDTO updated = discussionService.updateTopic(topicId, topicDTO);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/topics/{topicId}")
    public ResponseEntity<Void> deleteTopic(@PathVariable Long topicId) {
        discussionService.deleteTopic(topicId);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/topics/{topicId}/pin")
    public ResponseEntity<DiscussionTopicDTO> pinTopic(
            @PathVariable Long topicId,
            @RequestBody Map<String, Boolean> request) {
        DiscussionTopicDTO updated = discussionService.pinTopic(topicId, request.get("isPinned"));
        return ResponseEntity.ok(updated);
    }
    
    @PutMapping("/topics/{topicId}/lock")
    public ResponseEntity<DiscussionTopicDTO> lockTopic(
            @PathVariable Long topicId,
            @RequestBody Map<String, Boolean> request) {
        DiscussionTopicDTO updated = discussionService.lockTopic(topicId, request.get("isLocked"));
        return ResponseEntity.ok(updated);
    }
    
    @PutMapping("/topics/{topicId}/solve")
    public ResponseEntity<DiscussionTopicDTO> markAsSolved(
            @PathVariable Long topicId,
            @RequestBody Map<String, Boolean> request) {
        DiscussionTopicDTO updated = discussionService.markAsSolved(topicId, request.get("isSolved"));
        return ResponseEntity.ok(updated);
    }
    
    // Reply endpoints
    @PostMapping("/replies")
    public ResponseEntity<DiscussionReplyDTO> createReply(@RequestBody DiscussionReplyDTO replyDTO) {
        DiscussionReplyDTO created = discussionService.createReply(replyDTO);
        return ResponseEntity.ok(created);
    }
    
    @GetMapping("/replies/topic/{topicId}")
    public ResponseEntity<List<DiscussionReplyDTO>> getRepliesByTopic(
            @PathVariable Long topicId,
            @RequestParam Long currentUserId) {
        List<DiscussionReplyDTO> replies = discussionService.getRepliesByTopic(topicId, currentUserId);
        return ResponseEntity.ok(replies);
    }
    
    @PutMapping("/replies/{replyId}")
    public ResponseEntity<DiscussionReplyDTO> updateReply(
            @PathVariable Long replyId,
            @RequestBody DiscussionReplyDTO replyDTO) {
        DiscussionReplyDTO updated = discussionService.updateReply(replyId, replyDTO);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/replies/{replyId}")
    public ResponseEntity<Void> deleteReply(@PathVariable Long replyId) {
        discussionService.deleteReply(replyId);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/replies/{replyId}/solution")
    public ResponseEntity<DiscussionReplyDTO> markAsSolution(
            @PathVariable Long replyId,
            @RequestBody Map<String, Boolean> request) {
        DiscussionReplyDTO updated = discussionService.markAsSolution(replyId, request.get("isSolution"));
        return ResponseEntity.ok(updated);
    }
    
    // Like endpoints
    @PostMapping("/topics/{topicId}/like")
    public ResponseEntity<Void> toggleTopicLike(
            @PathVariable Long topicId,
            @RequestParam Long userId) {
        discussionService.toggleTopicLike(topicId, userId);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/replies/{replyId}/like")
    public ResponseEntity<Void> toggleReplyLike(
            @PathVariable Long replyId,
            @RequestParam Long userId) {
        discussionService.toggleReplyLike(replyId, userId);
        return ResponseEntity.ok().build();
    }
}
