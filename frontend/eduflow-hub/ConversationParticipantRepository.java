package com.elearnhub.teacher_service.repository;

import com.elearnhub.teacher_service.entity.ConversationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, Long> {
    
    // Find participants by conversation ID
    List<ConversationParticipant> findByConversationId(Long conversationId);
    
    // Find participant by conversation and user
    Optional<ConversationParticipant> findByConversationIdAndUserId(Long conversationId, Long userId);
    
    // Check if user is participant in conversation
    boolean existsByConversationIdAndUserId(Long conversationId, Long userId);
    
    // Find all conversations for a user
    @Query("SELECT cp FROM ConversationParticipant cp WHERE cp.user.id = :userId ORDER BY cp.conversation.updatedAt DESC")
    List<ConversationParticipant> findByUserId(@Param("userId") Long userId);
}