package com.example.PersonalBlog.controller;

/**
 * @author 赵进
 * @date 2025/9/17
 */


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.PersonalBlog.entity.Posts;
import com.example.PersonalBlog.entity.PostsVO;
import com.example.PersonalBlog.entity.User;
import com.example.PersonalBlog.mapper.PostsMapper;
import com.example.PersonalBlog.mapper.UserMapper;
import com.example.PersonalBlog.utils.JwtUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/posts")
public class PostsController {

    private final PostsMapper postsMapper;
    private final UserMapper userMapper;

    public PostsController(PostsMapper postsMapper, UserMapper userMapper) {
        this.postsMapper = postsMapper;
        this.userMapper = userMapper;
    }

    // 获取所有文章
    @GetMapping
    public ResponseEntity<?> getAllPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size) {

        Page<Posts> postPage = new Page<>(page, size);
        postsMapper.selectPage(postPage, null);

        List<PostsVO> result = new ArrayList<>();
        for (Posts post : postPage.getRecords()) {
            PostsVO vo = new PostsVO();
            BeanUtils.copyProperties(post, vo);

            // 转换时间
            if (post.getCreatedAt() != null) {
                vo.setCreatedAt(post.getCreatedAt());
            }

            // 查找作者
            if (post.getUserId() != null) {
                User user = userMapper.selectById(post.getUserId());
                if (user != null) {
                    vo.setAuthorName(user.getUsername());
                } else {
                    vo.setAuthorName("未知作者");
                }
            } else {
                vo.setAuthorName("匿名");
            }

            result.add(vo);
        }

        // 返回分页结构
        Map<String, Object> response = new HashMap<>();
        response.put("records", result);
        response.put("total", postPage.getTotal());

        return ResponseEntity.ok(response);
    }

   @PostMapping
   public ResponseEntity<?> createPost(@RequestBody Posts post, HttpServletRequest request) {
       String token = request.getHeader("Authorization");
       if (token != null && token.startsWith("Bearer ")) {
           token = token.substring(7);
       } else {
           return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("缺少或非法的 token");
       }

       String username;
       try {
           username = JwtUtils.getUsernameFromToken(token);
       } catch (Exception e) {
           return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("token 解析失败");
       }

       // 通过 username 查用户
       QueryWrapper<User> queryWrapper = new QueryWrapper<>();
       queryWrapper.eq("username", username);
       User user = userMapper.selectOne(queryWrapper);

       if (user == null) {
           return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("用户不存在");
       }

       // 设置 user_id 和创建时间
       post.setUserId(user.getId()); // ⚠️ 确保 User 实体中有 getId()
       post.setCreatedAt(LocalDateTime.now());

       int result = postsMapper.insert(post);
       if (result > 0) {
           return ResponseEntity.ok("文章发布成功");
       } else {
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("发布失败");
       }
   }

}

