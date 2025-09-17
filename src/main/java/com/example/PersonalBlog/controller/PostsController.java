package com.example.PersonalBlog.controller;

/**
 * @author 赵进
 * @date 2025/9/17
 */


import com.example.PersonalBlog.entity.Posts;
import com.example.PersonalBlog.mapper.PostsMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/posts")
public class PostsController {

    private final PostsMapper postsMapper;

    public PostsController(PostsMapper postsMapper) {
        this.postsMapper = postsMapper;
    }

    // 获取所有文章
    @GetMapping
    public ResponseEntity<?> getAllPosts() {
        List<Posts> posts = postsMapper.selectList(null);
        return ResponseEntity.ok(posts);
    }

    // 发布文章（假设不校验登录用户）
    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody Posts post) {
        post.setCreated_at(LocalDateTime.now().toString());
        int result = postsMapper.insert(post);
        if (result > 0) {
            return ResponseEntity.ok("文章发布成功");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("发布失败");
        }
    }
}

