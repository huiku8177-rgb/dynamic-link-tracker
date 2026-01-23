/*
package com.neozeng.trackerserve.filter;

import com.neozeng.trackerserve.pojo.User;
import com.neozeng.trackerserve.service.UserService;
import com.neozeng.trackerserve.util.JwtUtils;
import com.neozeng.trackerserve.util.UserHolder;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

*/
/**
 * 登录过滤器
 * 用于验证 JWT Token 并设置当前用户信息到 ThreadLocal
 * Filter 在 Interceptor 之前执行，更可靠
 *//*

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class LoginFilter implements Filter {

    private final JwtUtils jwtUtils;
    private final UserService userService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestPath = httpRequest.getRequestURI();
        String requestMethod = httpRequest.getMethod();

        // 排除不需要认证的路径
        if (shouldSkip(requestPath)) {
            chain.doFilter(request, response);
            return;
        }

        try {
            System.out.println("========================================");
            System.out.println("Filter 执行：路径=" + requestPath + ", 方法=" + requestMethod);

            // 1. 从请求头获取 Token
            String token = getTokenFromRequest(httpRequest);
            System.out.println("Token 是否存在：" + (token != null ? "是" : "否"));

            if (!StringUtils.hasText(token)) {
                System.out.println("未找到 Token，直接放行");
                UserHolder.removeUser();
                chain.doFilter(request, response);
                return;
            }

            // 2. 验证 Token
            boolean isValid = jwtUtils.validateToken(token);
            System.out.println("Token 验证结果：" + isValid);

            if (!isValid) {
                System.out.println("Token 验证失败");
                UserHolder.removeUser();
                chain.doFilter(request, response);
                return;
            }

            // 3. 从 Token 中获取用户 ID
            Long userId = jwtUtils.getUserIdFromToken(token);
            System.out.println("从 Token 解析出的 userId: " + userId);

            if (userId == null) {
                System.out.println("无法从 Token 中解析 userId");
                UserHolder.removeUser();
                chain.doFilter(request, response);
                return;
            }

            // 4. 查询用户信息并设置到 ThreadLocal
            User user = userService.findById(userId);
            if (user != null) {
                UserHolder.saveUser(user);
                System.out.println("✅ 用户信息已设置到 ThreadLocal：userId=" + userId + ", username=" + user.getUsername());
                log.info("✅ 用户信息已成功设置到 ThreadLocal：userId={}, username={}, email={}",
                        userId, user.getUsername(), user.getEmail());
            } else {
                System.out.println("❌ 用户不存在：userId=" + userId);
                UserHolder.removeUser();
            }

            System.out.println("========================================");

        } catch (Exception e) {
            System.out.println("Filter 执行异常：" + e.getMessage());
            e.printStackTrace();
            log.error("Filter 执行异常", e);
            UserHolder.removeUser();
        } finally {
            // Filter 链执行完后清理 ThreadLocal
            try {
                chain.doFilter(request, response);
            } finally {
                UserHolder.removeUser();
            }
        }
    }

    */
/**
     * 判断是否应该跳过该路径（不需要认证）
     *//*

    private boolean shouldSkip(String path) {
        // 排除认证相关接口
        if (path.startsWith("/api/auth")) {
            return true;
        }
        // 排除 Swagger 相关路径
        if (path.startsWith("/swagger") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/webjars") ||
                path.equals("/csrf")) {
            return true;
        }
        // 排除短链接跳转接口（如 /1, /a 等）
        if (path.matches("^/[^/]+$")) {
            return true;
        }
        // 排除静态资源
        return path.equals("/error") || path.equals("/favicon.ico");
    }


    */
/**
     * 从请求中获取 Token
     *//*

    private String getTokenFromRequest(HttpServletRequest request) {
        // 方式1：从 Authorization 头获取
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // 方式2：从 token 请求头获取
        String tokenHeader = request.getHeader("token");
        if (StringUtils.hasText(tokenHeader)) {
            return tokenHeader;
        }

        return null;
    }
}

*/
