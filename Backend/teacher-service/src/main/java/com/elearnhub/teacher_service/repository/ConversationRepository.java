package com.elearnhub.teacher_service.repository;

import com.elearnhub.teacher_service.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    
    // Find conversations by participant ID
    @Query("SELECT DISTINCT c FROM Conversation c " +
           "JOIN c.participantEntities p " +
           "WHERE p.user.id = :userId " +
           "ORDER BY c.updatedAt DESC")
    List<Conversation> findByParticipantId(@Param("userId") Long userId);
    
    // Find direct conversation between two users
    @Query("SELECT c FROM Conversation c " +
           "WHERE c.type = 'DIRECT' " +
           "AND EXISTS (SELECT 1 FROM ConversationParticipant p1 WHERE p1.conversation = c AND p1.user.id = :userId1) " +
           "AND EXISTS (SELECT 1 FROM ConversationParticipant p2 WHERE p2.conversation = c AND p2.user.id = :userId2)")
    Optional<Conversation> findDirectConversation(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
    
    // Find conversation by class ID
    Optional<Conversation> findByClassId(Long classId);
    
    // Find group conversations
    @Query("SELECT c FROM Conversation c WHERE c.type = 'GROUP' ORDER BY c.updatedAt DESC")
    List<Conversation> findGroupConversations();
}