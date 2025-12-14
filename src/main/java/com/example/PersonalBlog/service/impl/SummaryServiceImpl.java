package com.example.PersonalBlog.service.impl;

import com.example.PersonalBlog.entity.ChatMessage;
import com.example.PersonalBlog.entity.ChatSessionSummary;
import com.example.PersonalBlog.mapper.ChatMessageRepository;
import com.example.PersonalBlog.mapper.ChatSessionSummaryRepository;
import com.example.PersonalBlog.service.SummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SummaryServiceImpl implements SummaryService {

    private static final int MESSAGE_THRESHOLD = 10;
    private static final int TOKEN_THRESHOLD = 3000;
    private static final String OLLAMA_URL = "http://120.26.23.10:11434/api/chat";

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatSessionSummaryRepository summaryRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public void generateIfNeeded(Long sessionId) {

        // 1️⃣ 查询已有 summary
        ChatSessionSummary summary =
                summaryRepository.findBySessionId(sessionId).orElse(null);

        Long lastMessageId = summary != null ? summary.getLastMessageId() : null;

        // 2️⃣ 查询该 session 的所有消息
        List<ChatMessage> allMessages =
                chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);

        if (allMessages == null || allMessages.isEmpty()) {
            return;
        }

        // 3️⃣ 过滤出未被 summary 覆盖的消息
        List<ChatMessage> newMessages = new ArrayList<ChatMessage>();
        for (ChatMessage m : allMessages) {
            if (lastMessageId == null || m.getId() > lastMessageId) {
                newMessages.add(m);
            }
        }

        if (newMessages.isEmpty()) {
            return;
        }

        // 4️⃣ 判断是否触发 summary
        int tokenSum = 0;
        for (ChatMessage m : newMessages) {
            tokenSum += estimateTokens(m.getContent());
        }

        if (newMessages.size() < MESSAGE_THRESHOLD && tokenSum < TOKEN_THRESHOLD) {
            return;
        }

        // 5️⃣ 构建 summary prompt
        String prompt = buildSummaryPrompt(
                summary != null ? summary.getSummary() : null,
                newMessages
        );

        // 6️⃣ 调用 Ollama
        String newSummary = callOllama(prompt);
        if (newSummary == null || newSummary.trim().isEmpty()) {
            return;
        }

        // 7️⃣ 保存 summary
        ChatSessionSummary entity = summary != null ? summary : new ChatSessionSummary();
        entity.setSessionId(sessionId);
        entity.setSummary(newSummary);
        entity.setLastMessageId(
                newMessages.get(newMessages.size() - 1).getId()
        );

        summaryRepository.save(entity);
    }

    // ================== 工具方法 ==================

    /**
     * 粗略估算 token 数
     */
    private int estimateTokens(String text) {
        if (text == null) {
            return 0;
        }
        return text.length() / 3;
    }

    /**
     * 构建 summary prompt（Java 8 写法）
     */
    private String buildSummaryPrompt(String oldSummary, List<ChatMessage> messages) {
        StringBuilder sb = new StringBuilder();

        sb.append("请将以下对话总结为“长期记忆摘要”，要求：\n");
        sb.append("要求：\n");
        sb.append("1. 用于指导后续 AI 回答风格\n");
        sb.append("2. 提炼用户身份、目标、偏好\n");
        sb.append("3. 使用简洁、指令化表达\n");
        sb.append("4. 不要逐条复述对话\n");

        if (oldSummary != null && oldSummary.trim().length() > 0) {
            sb.append("\n【已有摘要】\n");
            sb.append(oldSummary).append("\n");
        }

        sb.append("\n【新增对话】\n");
        for (ChatMessage m : messages) {
            sb.append(m.getRole())
                    .append(": ")
                    .append(m.getContent())
                    .append("\n");
        }

        return sb.toString();
    }

    /**
     * 调用 Ollama 生成 summary
     */
    private String callOllama(String prompt) {

        Map<String, Object> body = new HashMap<String, Object>();
        body.put("model", "qwen3:4b");
        body.put("stream", Boolean.FALSE);

        List<Map<String, String>> messages = new ArrayList<Map<String, String>>();
        Map<String, String> sysMsg = new HashMap<String, String>();
        sysMsg.put("role", "system");
        sysMsg.put("content", prompt);
        messages.add(sysMsg);

        body.put("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<Map<String, Object>>(body, headers);

        try {
            ResponseEntity<Map> response =
                    restTemplate.postForEntity(OLLAMA_URL, request, Map.class);

            if (response.getBody() == null) {
                return null;
            }

            Object msgObj = response.getBody().get("message");
            if (!(msgObj instanceof Map)) {
                return null;
            }

            Map msg = (Map) msgObj;
            Object content = msg.get("content");

            return content != null ? content.toString() : null;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
