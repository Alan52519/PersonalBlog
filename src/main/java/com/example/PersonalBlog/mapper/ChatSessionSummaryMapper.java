package com.example.PersonalBlog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.PersonalBlog.entity.ChatSessionSummary;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatSessionSummaryMapper
        extends BaseMapper<ChatSessionSummary> {
}

