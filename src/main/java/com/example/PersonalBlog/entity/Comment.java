package com.example.PersonalBlog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author 赵进
 * @date 2025/11/28
 */
@Data
@TableName("comments")
public class Comment {
    private Long id;
    private Long postId;
    private Long userId;
    private String content;
    private LocalDateTime createdAt;
}