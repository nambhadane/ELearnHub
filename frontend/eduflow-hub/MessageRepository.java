// ============================================
// FIXED MessageRepository - Order ASC for WhatsApp-style chat
// ============================================

package com.elearnhub.teacher_service.repository;

import com.elearnhub.teacher_service.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    // For getting last message in conversation
    List<Message> findByConversationIdOrderByCreatedAtDesc(Long conversationId);
    
    // ✅ FIX: Order ASC so oldest messages are first, newest at bottom (WhatsApp style)
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId ORDER BY m.createdAt ASC")
    List<Message> findByConversationIdOrdered(@Param("conversationId") Long conversationId, Pageable pageable);
}

