package com.elearnhub.teacher_service.repository;

import java.awt.print.Pageable;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.elearnhub.teacher_service.entity.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    // For getting last message in conversation
    List<Message> findByConversationIdOrderByCreatedAtDesc(Long conversationId);
    
    // ✅ FIX: Order ASC so oldest messages are first, newest at bottom (WhatsApp style)
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId ORDER BY m.createdAt ASC")
    List<Message> findByConversationIdOrdered(@Param("conversationId") Long conversationId, org.springframework.data.domain.Pageable pageable);
}
