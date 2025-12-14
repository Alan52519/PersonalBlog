package com.example.PersonalBlog.controller;

import com.example.PersonalBlog.entity.ChatMessage;
import com.example.PersonalBlog.entity.ChatSession;
import com.example.PersonalBlog.entity.ChatSessionSummary;
import com.example.PersonalBlog.mapper.ChatMessageRepository;
import com.example.PersonalBlog.mapper.ChatSessionRepository;
import com.example.PersonalBlog.mapper.ChatSessionSummaryRepository;
import com.example.PersonalBlog.service.SummaryService;
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
import java.util.ArrayList;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Autowired
    private HttpServletRequest request;
    @Autowired
    private SummaryService summaryService;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ChatSessionSummaryRepository chatSessionSummaryRepository;

    public ChatController(ChatSessionRepository chatSessionRepository,
                          ChatMessageRepository chatMessageRepository) {
        this.chatSessionRepository = chatSessionRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    @PostMapping
    public ResponseEntity<?> chat(@RequestBody Map<String, Object> body) {
        try {
            // 1. 获取或创建对话主题ID
            Long sessionId;
            ChatSession session;

            Object sessionIdObj = body.get("sessionId");

            if (sessionIdObj != null && sessionIdObj.toString().trim().length() > 0) {
                sessionId = Long.valueOf(sessionIdObj.toString());
                session = chatSessionRepository.findById(sessionId).orElse(null);

                if (session == null) {
                    // 当作新对话
                    String token = request.getHeader("Authorization").replace("Bearer ", "");
                    Long userId = JwtUtils.getUserIdFromToken(token);

                    session = new ChatSession();
                    session.setUserId(userId);
                    session.setTitle("默认对话");
                    session = chatSessionRepository.save(session);
                    sessionId = session.getId();
                }
            } else {
                // 新建 session
                String authHeader = request.getHeader("Authorization");
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    throw new RuntimeException("缺少 Authorization 头");
                }

                String token = authHeader.replace("Bearer ", "");
                Long userId = JwtUtils.getUserIdFromToken(token);
                if (userId == null) {
                    throw new RuntimeException("无法从 token 获取 userId");
                }

                session = new ChatSession();
                session.setUserId(userId);
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

            // 3. 调用 Ollama

            String url = "http://120.26.23.10:11434/api/chat";

            // ===== 构建带记忆的 Ollama messages =====
            List<Map<String, String>> ollamaMessages = new ArrayList<Map<String, String>>();

            // 1. system prompt
            Map<String, String> systemPrompt = new HashMap<String, String>();
            systemPrompt.put("role", "system");
            systemPrompt.put("content", "你是一个 AI 助手。\n" +
                    "如果我提供了【历史摘要】，你可以参考它；\n" +
                    "如果没有提供历史摘要，这是全新的对话。");
            ollamaMessages.add(systemPrompt);

            // 2. 注入 summary（长期记忆）
            ChatSessionSummary summary =
                    chatSessionSummaryRepository.findBySessionId(sessionId).orElse(null);

            if (summary != null
                    && summary.getSummary() != null
                    && summary.getSummary().trim().length() > 0) {

                Map<String, String> summaryMsg = new HashMap<String, String>();
                summaryMsg.put("role", "system");
                summaryMsg.put("content", "【历史摘要】\n" + summary.getSummary());
                ollamaMessages.add(summaryMsg);
            }

            // 3. 最近 N 条消息（短期记忆）
            List<ChatMessage> recentMessages =
                    chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);

            // 只取最近 10 条
            int start = Math.max(0, recentMessages.size() - 10);
            for (int i = start; i < recentMessages.size(); i++) {
                ChatMessage msg = recentMessages.get(i);
                Map<String, String> m = new HashMap<String, String>();
                m.put("role", msg.getRole().name());
                m.put("content", msg.getContent());
                ollamaMessages.add(m);
            }

            // 4. 覆盖 body 中的 messages（⚠️ 一定在这里）
            body.put("messages", ollamaMessages);

            // 5. 再创建 HttpEntity（顺序很重要）
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> requestEntity =
                    new HttpEntity<Map<String, Object>>(body, headers);

            // 6. 调用 Ollama
            ResponseEntity<Map> response =
                    restTemplate.postForEntity(url, requestEntity, Map.class);


            boolean assistantSaved = false;
            // 4. 保存助手消息

            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("message")) {
                Map<String, Object> messageMap =
                        (Map<String, Object>) responseBody.get("message");
                String assistantContent = (String) messageMap.get("content");

                if (assistantContent != null && assistantContent.trim().length() > 0) {
                    ChatMessage assistantMessage = new ChatMessage();
                    assistantMessage.setSession(session);
                    assistantMessage.setRole(ChatMessage.Role.assistant);
                    assistantMessage.setContent(assistantContent);
                    assistantMessage.setCreatedAt(LocalDateTime.now());
                    chatMessageRepository.save(assistantMessage);
                    assistantSaved = true;
                }
            }

            // 5. 触发 summary（只在 assistant 保存成功后）
            if (assistantSaved) {
                summaryService.generateIfNeeded(sessionId);
            }

            // 6. 返回 Ollama 原始响应 + sessionId
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