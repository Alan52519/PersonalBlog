package com.example.PersonalBlog.controller;

/**
 * @author 赵进
 * @date 2025/9/15
 */

import com.example.PersonalBlog.entity.User;
import com.example.PersonalBlog.mapper.UserMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {

    private final UserMapper userMapper;

    public UserController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userMapper.selectList(null); // 查询所有用户
    }
}
