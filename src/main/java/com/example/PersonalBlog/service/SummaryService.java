package com.example.PersonalBlog.service;

import com.example.PersonalBlog.entity.ChatSessionSummary;

public interface SummaryService {

    /**
     * 判断并生成/更新 summary（如果需要）
     */
    void generateIfNeeded(Long sessionId);
}
