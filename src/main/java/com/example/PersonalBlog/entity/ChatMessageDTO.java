package com.example.PersonalBlog.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ChatMessageDTO {
    private Long id;
    private String role;
    private String content;
    private LocalDateTime createdAt;
}