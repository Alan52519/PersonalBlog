package com.example.PersonalBlog.controller;

/**
 * @author 赵进
 * @date 2025/9/16
 */

import com.example.PersonalBlog.entity.User;
import com.example.PersonalBlog.mapper.UserMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthController(UserMapper userMapper, BCryptPasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public String register(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_USER");
        userMapper.insert(user);
        return "注册成功";
    }
}

