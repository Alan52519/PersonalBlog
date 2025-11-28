package com.example.PersonalBlog.entity;

/**
 * @author 赵进
 * @date 2025/9/18
 */
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostsVO {

    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private Long userId;
    private String authorName;
    private Integer likes;
}
