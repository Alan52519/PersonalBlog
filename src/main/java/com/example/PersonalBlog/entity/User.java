package com.example.PersonalBlog.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * @author 赵进
 * @date 2025/9/15
 */
@TableName("users")
public class User {
    @TableId
    private Long id;

    private String username;
    private Integer password;
    private String role;

    // Getter 和 Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return username;
    }

    public void setName(String name) {
        this.username = name;
    }

    public Integer getAge() {
        return password;
    }

    public void setAge(Integer age) {
        this.password = age;
    }

    public String getEmail() {
        return role;
    }

    public void setEmail(String email) {
        this.role = email;
    }
}
