package com.example.PersonalBlog.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author 赵进
 * @date 2025/9/15
 */
@Data
@TableName("users")
public class User {
    private String username;
    private String password;
    private String role;

}
