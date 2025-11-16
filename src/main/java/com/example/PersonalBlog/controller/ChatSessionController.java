package com.example.PersonalBlog.controller;

import com.example.PersonalBlog.entity.ChatMessageDTO;
import com.example.PersonalBlog.entity.ChatSession;
import com.example.PersonalBlog.entity.ChatSessionDTO;
import com.example.PersonalBlog.entity.User;
import com.example.PersonalBlog.mapper.ChatSessionRepository;
import com.example.PersonalBlog.utils.JwtUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/session")
public class ChatSessionController {

    private final ChatSessionRepository chatSessionRepository;

    public ChatSessionController(ChatSessionRepository chatSessionRepository) {
        this.chatSessionRepository = chatSessionRepository;
    }

    // 获取用户所有会话
    @GetMapping("/getSessions")
    public ResponseEntity<List<ChatSessionDTO>> getSessions(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").replace("Bearer ", "");
            Long userId = JwtUtils.getUserIdFromToken(token);

            List<ChatSession> sessions = chatSessionRepository.findByUserId(userId);

            List<ChatSessionDTO> dtos = sessions.stream().map(s -> {
                List<ChatMessageDTO> msgs = s.getMessages() == null ? new ArrayList<>() :
                        s.getMessages().stream()
                                .map(m -> new ChatMessageDTO(m.getId(), m.getRole().name(), m.getContent(), m.getCreatedAt()))
                                .collect(Collectors.toList());
                return new ChatSessionDTO(s.getId(), s.getTitle(), msgs);
            }).collect(Collectors.toList());

            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 修改会话标题
    @PutMapping("/{id}/title")
    public ResponseEntity<?> updateTitle(@PathVariable Long id,
                                         @RequestBody Map<String, String> body,
                                         HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").replace("Bearer ", "");
            Long userId = JwtUtils.getUserIdFromToken(token);

            ChatSession session = chatSessionRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Session不存在"));

            if (!session.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("不能修改别人的会话");
            }

            String newTitle = body.get("title");
            session.setTitle(newTitle);
            chatSessionRepository.save(session);

            return ResponseEntity.ok(session);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("修改失败");
        }
    }

    // 删除会话
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSession(@PathVariable Long id,
                                           HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").replace("Bearer ", "");
            Long userId = JwtUtils.getUserIdFromToken(token);

            ChatSession session = chatSessionRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Session不存在"));

            if (!session.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("不能删除别人的会话");
            }

            chatSessionRepository.delete(session);

            return ResponseEntity.ok("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("删除失败");
        }
    }
}