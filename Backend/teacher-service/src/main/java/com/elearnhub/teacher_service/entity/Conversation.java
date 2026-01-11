package com.elearnhub.teacher_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "conversations")
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ConversationType type; // DIRECT or GROUP

    @Column(name = "name")
    private String name; // For group chats (e.g., "Web Development - Section A")

    @Column(name = "class_id")
    private Long classId; // For class-based group chats

    // ✅ FIX: Remove @ManyToMany - use ConversationParticipant entity instead
    // This was causing conflict with ConversationParticipant entity
    @OneToMany(mappedBy = "conversation", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConversationParticipant> participantEntities = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages = new ArrayList<>();

    public enum ConversationType {
        DIRECT,  // 1-on-1 chat
        GROUP    // Group chat (class-based or custom)
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ========== GETTERS ==========
    public Long getId() {
        return id;
    }

    public ConversationType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Long getClassId() {
        return classId;
    }

    public List<ConversationParticipant> getParticipantEntities() {
        return participantEntities;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<Message> getMessages() {
        return messages;
    }

    // ========== SETTERS ==========
    public void setId(Long id) {
        this.id = id;
    }

    public void setType(ConversationType type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public void setParticipantEntities(List<ConversationParticipant> participantEntities) {
        this.participantEntities = participantEntities;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}