package com.example.PersonalBlog.service;

import com.example.PersonalBlog.entity.ChatMessage;
import com.example.PersonalBlog.entity.ChatSession;
import com.example.PersonalBlog.mapper.ChatMessageRepository;
import com.example.PersonalBlog.mapper.ChatSessionRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class ChatService {

    private final ChatSessionRepository sessionRepo;
    private final ChatMessageRepository messageRepo;

    public ChatService(ChatSessionRepository sessionRepo, ChatMessageRepository messageRepo) {
        this.sessionRepo = sessionRepo;
        this.messageRepo = messageRepo;
    }

    // 创建新会话
    public ChatSession createSession(Long userId, String title) {
        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setTitle(title);
        return sessionRepo.save(session);
    }

    // 发送消息
    public ChatMessage sendMessage(Long sessionId, String userMessage) throws Exception {
        ChatSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        // 保存用户消息
        ChatMessage userMsg = new ChatMessage();
        userMsg.setSession(session);
        userMsg.setRole(ChatMessage.Role.user);
        userMsg.setContent(userMessage);
        messageRepo.save(userMsg);

        // 调用本地 Ollama
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://127.0.0.1:11434/api/chat"; // Ollama 本地 API

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String body = String.format(
                "{\"model\":\"qwen3:4b\",\"messages\":[{\"role\":\"user\",\"content\":\"%s\"}],\"stream\":false}",
                userMessage
        );

        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        // 解析返回 JSON (简单示例)
        String assistantText = response.getBody(); // 可以用 Jackson 解析 data.message.content

        // 保存 AI 消息
        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setSession(session);
        aiMsg.setRole(ChatMessage.Role.assistant);
        aiMsg.setContent(assistantText);
        messageRepo.save(aiMsg);

        return aiMsg;
    }

    public List<ChatMessage> getMessages(Long sessionId) {
        return messageRepo.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }
}