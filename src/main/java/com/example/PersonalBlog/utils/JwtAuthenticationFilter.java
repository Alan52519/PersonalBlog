package com.example.PersonalBlog.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collections;
/**
 * @author 赵进
 * @date 2025/9/17
 */

public class JwtAuthenticationFilter implements Filter {

    private static final String SECRET_KEY = "your-secret-key";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String token = getTokenFromRequest(httpRequest);

        // ✅ 打印 token 方便调试
        System.out.println("JWT token: " + token);

        if (StringUtils.hasText(token)) {
            try {
                Claims claims = Jwts.parser()
                        .setSigningKey(SECRET_KEY)
                        .parseClaimsJws(token)
                        .getBody();

                Long userId = claims.get("userId", Long.class);
                String username = claims.getSubject();
                String role = claims.get("role", String.class);

                // 如果你不需要角色判断，可以用 emptyList()
                // 如果需要角色控制，可以改成 Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                Collections.emptyList() // 或者根据角色生成权限列表
                        );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpRequest));

                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                System.out.println("JWT解析失败: " + e.getMessage());
            }
        }

        chain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
