package com.example.PersonalBlog.mapper;

import com.example.PersonalBlog.entity.ChatSessionSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatSessionSummaryRepository
        extends JpaRepository<ChatSessionSummary, Long> {

    Optional<ChatSessionSummary> findBySessionId(Long sessionId);
}
