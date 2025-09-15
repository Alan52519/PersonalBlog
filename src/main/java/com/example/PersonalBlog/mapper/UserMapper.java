package com.example.PersonalBlog.mapper;

/**
 * @author 赵进
 * @date 2025/9/15
 */

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.PersonalBlog.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 不需要写任何 SQL，MyBatis-Plus 自动实现基本增删改查
}