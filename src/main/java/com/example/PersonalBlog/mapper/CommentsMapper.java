package com.example.PersonalBlog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.PersonalBlog.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author 赵进
 * @date 2025/11/28
 */
@Mapper
public interface CommentsMapper extends BaseMapper<Comment> {}