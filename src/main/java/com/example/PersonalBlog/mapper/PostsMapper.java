package com.example.PersonalBlog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.PersonalBlog.entity.Posts;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author 赵进
 * @date 2025/9/17
 */
@Mapper
public interface PostsMapper extends BaseMapper<Posts> {
}
