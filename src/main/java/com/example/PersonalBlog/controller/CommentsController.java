package com.example.PersonalBlog.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.PersonalBlog.entity.Comment;
import com.example.PersonalBlog.entity.CommentVO;
import com.example.PersonalBlog.entity.User;
import com.example.PersonalBlog.mapper.CommentsMapper;
import com.example.PersonalBlog.mapper.UserMapper;
import com.example.PersonalBlog.utils.JwtUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author 赵进
 * @date 2025/11/28
 */
@RestController
@RequestMapping("/posts")
public class CommentsController {

    private final CommentsMapper commentsMapper;
    private final UserMapper userMapper;

    public CommentsController(CommentsMapper commentsMapper, UserMapper userMapper) {
        this.commentsMapper = commentsMapper;
        this.userMapper = userMapper;
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<?> addComment(
            @PathVariable Long postId,
            @RequestBody Map<String, String> requestBody,
            HttpServletRequest request
    ) {
        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("缺少 token");
        }
        token = token.substring(7);

        Long userId;
        try {
            userId = JwtUtils.getUserIdFromToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("token 无效");
        }

        String content = requestBody.get("content");
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("评论内容不能为空");
        }

        Comment c = new Comment();
        c.setPostId(postId);
        c.setUserId(userId);
        c.setContent(content);
        c.setCreatedAt(LocalDateTime.now());

        commentsMapper.insert(c);

        return ResponseEntity.ok("评论成功");
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<?> getComments(@PathVariable Long postId) {

        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("post_id", postId);
        wrapper.orderByAsc("created_at");

        List<Comment> comments = commentsMapper.selectList(wrapper);

        List<CommentVO> result = new ArrayList<>();
        for (Comment c : comments) {
            CommentVO vo = new CommentVO();
            BeanUtils.copyProperties(c, vo);

            User user = userMapper.selectById(c.getUserId());
            vo.setUsername(user != null ? user.getUsername() : "未知用户");

            result.add(vo);
        }

        return ResponseEntity.ok(result);
    }
}
