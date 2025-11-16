package com.example.PersonalBlog.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "session_id")
    private ChatSession session;

    @Enumerated(EnumType.STRING)
    private Role role; // USER / ASSISTANT

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Role {
        user, assistant
    }

    // getter/setter
}
