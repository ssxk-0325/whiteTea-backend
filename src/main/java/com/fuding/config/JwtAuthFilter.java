package com.fuding.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fuding.common.Result;
import com.fuding.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * JWT 认证过滤器：对需要登录的接口，在进入 Controller 前校验 token。
 * token 缺失、无效或过期时直接返回 HTTP 401，与常见项目做法一致。
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/user/login",
            "/user/register"
    );

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();
        // 去掉 context-path（如 /api）后再判断
        String path = uri;
        if (path.startsWith("/api")) {
            path = path.substring(4);
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        boolean isPublic = PUBLIC_PATHS.stream().anyMatch(path::contains);
        if (isPublic) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        String token = null;
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        // 无 token 时放行，由各 Controller 内校验（避免误伤公开接口）
        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            jwtUtil.getUserIdFromToken(token);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            send401(response, e.getMessage() != null ? e.getMessage() : "未授权");
        }
    }

    private void send401(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        Result<?> result = Result.error(401, message);
        response.getWriter().write(new ObjectMapper().writeValueAsString(result));
    }
}
