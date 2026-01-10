// ============================================
// FIXED ConversationRepository - Update queries to use ConversationParticipant
// ============================================

package com.elearnhub.teacher_service.repository;

import com.elearnhub.teacher_service.entity.Conversation;
import com.elearnhub.teacher_service.entity.Message;
import com.elearnhub.teacher_service.entity.ConversationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    
    // ✅ FIX: Query through ConversationParticipant entities, not participants collection
    @Query("SELECT DISTINCT c FROM Conversation c " +
           "JOIN ConversationParticipant cp ON cp.conversation.id = c.id " +
           "WHERE cp.user.id = :userId " +
           "ORDER BY c.updatedAt DESC")
    List<Conversation> findByParticipantId(@Param("userId") Long userId);
    
    Optional<Conversation> findByClassId(Long classId);
    
    // ✅ FIX: Query for direct conversation using ConversationParticipant
    @Query("SELECT DISTINCT c FROM Conversation c " +
           "WHERE c.type = 'DIRECT' " +
           "AND EXISTS (SELECT 1 FROM ConversationParticipant cp1 WHERE cp1.conversation.id = c.id AND cp1.user.id = :userId1) " +
           "AND EXISTS (SELECT 1 FROM ConversationParticipant cp2 WHERE cp2.conversation.id = c.id AND cp2.user.id = :userId2) " +
           "AND (SELECT COUNT(cp3) FROM ConversationParticipant cp3 WHERE cp3.conversation.id = c.id) = 2")
    Optional<Conversation> findDirectConversation(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversationIdOrderByCreatedAtDesc(Long conversationId);
    
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId ORDER BY m.createdAt DESC")
    List<Message> findByConversationIdOrdered(@Param("conversationId") Long conversationId, Pageable pageable);
}

@Repository
public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, Long> {
    Optional<ConversationParticipant> findByConversationIdAndUserId(Long conversationId, Long userId);
    
    List<ConversationParticipant> findByConversationId(Long conversationId);
    
    boolean existsByConversationIdAndUserId(Long conversationId, Long userId);
}

