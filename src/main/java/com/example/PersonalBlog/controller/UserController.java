package com.example.PersonalBlog.controller;

/**
 * @author 赵进
 * @date 2025/9/15
 */

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.PersonalBlog.entity.User;
import com.example.PersonalBlog.mapper.UserMapper;
import com.example.PersonalBlog.utils.JwtUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class UserController {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserController(UserMapper userMapper) {
        this.userMapper = userMapper;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        // 根据用户名查用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("用户名不存在");
        }

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("密码错误");
        }

        // 登录成功，生成 token（JWT）
        String token = JwtUtils.generateToken(user); // 你需要实现 JwtUtils 工具类

        // 返回 token
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("message", "登录成功");
        return ResponseEntity.ok(response);
    }
}

