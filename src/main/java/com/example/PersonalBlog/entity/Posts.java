package com.example.PersonalBlog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author 赵进
 * @date 2025/9/17
 */
@Data
@TableName("posts")
public class Posts {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private Long userId;
}
