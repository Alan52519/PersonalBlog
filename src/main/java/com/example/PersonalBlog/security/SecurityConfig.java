package com.example.PersonalBlog.security;

/**
 * @author 赵进
 * @date 2025/9/16
 */

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final MyUserDetailsService userDetailsService;

    public SecurityConfig(MyUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    // 注册 BCryptPasswordEncoder
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 配置自定义 UserDetailsService 和密码加密器
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    // 配置路径访问权限
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors()                     // 先启用 CORS
                .and()
                .csrf().disable()           // 再禁用 CSRF
                .authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .antMatchers("/register", "/login").permitAll()
                .antMatchers("/admin/**").hasRole("ADMIN")
                .antMatchers("/user/**").hasAnyRole("USER","ADMIN")
                .anyRequest().authenticated()
                .and()
                .formLogin().disable();     // 关闭默认表单
    }


    // 供外部注入 AuthenticationManager
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

}