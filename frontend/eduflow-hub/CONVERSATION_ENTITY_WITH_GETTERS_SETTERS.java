// ============================================
// Conversation Entity with Explicit Getters/Setters
// Use this if Lombok @Data is not working
// ============================================

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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "conversation_participants",
        joinColumns = @JoinColumn(name = "conversation_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> participants = new ArrayList<>();

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

    public List<User> getParticipants() {
        return participants;
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

    public void setParticipants(List<User> participants) {
        this.participants = participants;
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

    // ========== LIFECYCLE CALLBACKS ==========
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

// ============================================
// Message Entity with Explicit Getters/Setters
// ============================================

package com.elearnhub.teacher_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(name = "content", nullable = false, length = 2000)
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    // ========== GETTERS ==========
    public Long getId() {
        return id;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public User getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    // ========== SETTERS ==========
    public void setId(Long id) {
        this.id = id;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }

    // ========== LIFECYCLE CALLBACKS ==========
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

// ============================================
// ConversationParticipant Entity with Explicit Getters/Setters
// ============================================

package com.elearnhub.teacher_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "conversation_participants")
public class ConversationParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "last_read_at")
    private LocalDateTime lastReadAt;

    @Column(name = "unread_count", nullable = false)
    private Integer unreadCount = 0;

    // ========== GETTERS ==========
    public Long getId() {
        return id;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public User getUser() {
        return user;
    }

    public LocalDateTime getLastReadAt() {
        return lastReadAt;
    }

    public Integer getUnreadCount() {
        return unreadCount;
    }

    // ========== SETTERS ==========
    public void setId(Long id) {
        this.id = id;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setLastReadAt(LocalDateTime lastReadAt) {
        this.lastReadAt = lastReadAt;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }
}

