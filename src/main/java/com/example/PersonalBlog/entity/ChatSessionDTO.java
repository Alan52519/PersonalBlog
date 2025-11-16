package com.example.PersonalBlog.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class ChatSessionDTO {
    private Long id;
    private String title;
    private List<ChatMessageDTO> messages;
}


