package com.example.PersonalBlog.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "chat_session_summaries")
public class ChatSessionSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "session_id", nullable = false, unique = true)
    private ChatSession session;

    @Column(
            name = "session_id",
            insertable = false,
            updatable = false
    )
    private Long sessionId;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "last_message_id")
    private Long lastMessageId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
