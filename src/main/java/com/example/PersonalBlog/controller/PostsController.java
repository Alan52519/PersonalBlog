package com.example.PersonalBlog.controller;

/**
 * @author 赵进
 * @date 2025/9/17
 */


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.PersonalBlog.entity.*;
import com.example.PersonalBlog.mapper.CommentsMapper;
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
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(required = false) String keyword) { // 1. 新增 keyword 参数

        // 构建分页对象
        Page<Posts> postPage = new Page<>(page, size);

        // 2. 构建查询条件 Wrapper
        QueryWrapper<Posts> queryWrapper = new QueryWrapper<>();

        // 建议：按创建时间倒序排列（最新发布的在前面）
        queryWrapper.orderByDesc("created_at");

        // 3. 核心搜索逻辑：如果 keyword 存在且不为空，拼接查询条件
        if (keyword != null && !keyword.trim().isEmpty()) {
            // 生成 SQL 类似: AND (title LIKE '%keyword%' OR content LIKE '%keyword%')
            queryWrapper.and(wrapper ->
                    wrapper.like("title", keyword)
                            .or()
                            .like("content", keyword)
            );
        }

        // 4. 执行查询 (传入 queryWrapper 而不是 null)
        postsMapper.selectPage(postPage, queryWrapper);

        // --- 以下保持您原有的 VO 转换逻辑不变 ---

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

    @GetMapping("/{id}")
    public ResponseEntity<?> getPostById(@PathVariable Long id) {

        Posts post = postsMapper.selectById(id);
        if (post == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("文章不存在");
        }

        // 构建返回对象
        PostsVO vo = new PostsVO();
        BeanUtils.copyProperties(post, vo);

        // 设置作者名
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

        return ResponseEntity.ok(vo);
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<?> likePost(
            @PathVariable Long id,
            HttpServletRequest request) {

        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("缺少 token");
        }

        token = token.substring(7);
        Long userId;

        try {
            userId = JwtUtils.getUserIdFromToken(token); // 你要有这个方法
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("token 无效");
        }

        Posts post = postsMapper.selectById(id);
        if (post == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("文章不存在");
        }

        // 点赞 +1
        post.setLikes(post.getLikes() + 1);
        postsMapper.updateById(post);

        return ResponseEntity.ok(post.getLikes());
    }







    @PutMapping("/{id}")
    public ResponseEntity<?> updatePost(@PathVariable Long id, @RequestBody Posts post, HttpServletRequest request) {
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

        // 查找文章
        Posts existingPost = postsMapper.selectById(id);
        if (existingPost == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("文章不存在");
        }

        // 检查用户是否是作者
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (!existingPost.getUserId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("无权修改");
        }

        existingPost.setTitle(post.getTitle());
        existingPost.setContent(post.getContent());
        postsMapper.updateById(existingPost);

        return ResponseEntity.ok("修改成功");
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id, HttpServletRequest request) {
        // 1. 获取 Token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("缺少或非法的 token");
        }

        // 2. 解析用户名
        String username;
        try {
            username = JwtUtils.getUsernameFromToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("token 解析失败");
        }

        // 3. 查用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("用户不存在");
        }

        // 4. 查文章
        Posts post = postsMapper.selectById(id);
        if (post == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("文章不存在");
        }

        // 5. 权限校验：只能删自己的文章
        if (!post.getUserId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("你不能删除别人的文章");
        }

        // 6. 执行删除
        int result = postsMapper.deleteById(id);
        if (result > 0) {
            return ResponseEntity.ok("删除成功");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("删除失败");
        }
    }

}

