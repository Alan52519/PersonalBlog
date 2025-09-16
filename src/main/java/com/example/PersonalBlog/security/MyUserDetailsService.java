package com.example.PersonalBlog.security;

/**
 * @author 赵进
 * @date 2025/9/16
 */

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.PersonalBlog.entity.User;
import com.example.PersonalBlog.mapper.UserMapper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class MyUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;

    public MyUserDetailsService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User dbUser = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (dbUser == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

        // 构建 Spring Security 的 User 对象
        return new org.springframework.security.core.userdetails.User(
                dbUser.getUsername(),
                dbUser.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(dbUser.getRole()))
        );
    }
}

