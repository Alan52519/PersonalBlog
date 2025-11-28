package com.example.PersonalBlog.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author 赵进
 * @date 2025/11/28
 */
@Data
public class CommentVO {
    private Long id;
    private Long postId;
    private Long userId;
    private String content;
    private LocalDateTime createdAt;
    private String username;  // 评论者名
}
