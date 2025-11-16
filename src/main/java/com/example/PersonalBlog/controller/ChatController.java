package com.example.PersonalBlog.controller;

import com.example.PersonalBlog.entity.ChatMessage;
import com.example.PersonalBlog.entity.ChatSession;
import com.example.PersonalBlog.mapper.ChatMessageRepository;
import com.example.PersonalBlog.mapper.ChatSessionRepository;
import com.example.PersonalBlog.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final RestTemplate restTemplate;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Autowired
    private HttpServletRequest request;

    public ChatController(RestTemplateBuilder builder,
                          ChatSessionRepository chatSessionRepository,
                          ChatMessageRepository chatMessageRepository) {
        this.restTemplate = builder.build();
        this.chatSessionRepository = chatSessionRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    @PostMapping
    public ResponseEntity<?> chat(@RequestBody Map<String, Object> body) {
        try {
            // 1. 获取或创建对话主题ID
            Long sessionId;
            ChatSession session;

            if (body.containsKey("sessionId")) {
                sessionId = Long.valueOf(body.get("sessionId").toString());
                session = chatSessionRepository.findById(sessionId)
                        .orElseThrow(() -> new RuntimeException("Session不存在"));
            } else {
                // 从 token 获取 userId
                String token = request.getHeader("Authorization").replace("Bearer ", "");
                Long userId = JwtUtils.getUserIdFromToken(token);
                if (userId == null) {
                    throw new RuntimeException("无法从 token 获取 userId");
                }

                session = new ChatSession();
                session.setUserId(userId);           // 必须
                session.setTitle("默认对话");
                session = chatSessionRepository.save(session);
                sessionId = session.getId();
            }

            // 2. 保存用户消息
            List<Map<String, String>> messages = (List<Map<String, String>>) body.get("messages");
            if (messages != null && !messages.isEmpty()) {
                String userMessage = messages.get(0).get("content");
                ChatMessage userChatMessage = new ChatMessage();
                userChatMessage.setSession(session);        // ★ 关联 session
                userChatMessage.setRole(ChatMessage.Role.user);
                userChatMessage.setContent(userMessage);
                userChatMessage.setCreatedAt(LocalDateTime.now());
                chatMessageRepository.save(userChatMessage);
            }

            // 3. 调用 Ollama
            String url = "http://192.168.31.142:11434/api/chat";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);

            // 4. 保存助手消息
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("message")) {
                Map<String, Object> messageMap = (Map<String, Object>) responseBody.get("message");
                String assistantContent = (String) messageMap.get("content");

                if (assistantContent != null) {
                    ChatMessage assistantMessage = new ChatMessage();
                    assistantMessage.setSession(session);   // ★ 关联 session
                    assistantMessage.setRole(ChatMessage.Role.assistant);
                    assistantMessage.setContent(assistantContent);
                    assistantMessage.setCreatedAt(LocalDateTime.now());
                    chatMessageRepository.save(assistantMessage);
                }
            }

            // 5. 返回 Ollama 原始响应 + sessionId
            Map<String, Object> result = new HashMap<>();
            result.put("sessionId", sessionId);
            result.put("response", responseBody);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "调用 Ollama 出错: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}