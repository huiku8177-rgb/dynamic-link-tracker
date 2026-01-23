package com.neozeng.trackerserve.interceptor;

import com.neozeng.trackerserve.pojo.User;
import com.neozeng.trackerserve.service.UserService;
import com.neozeng.trackerserve.util.JwtUtils;
import com.neozeng.trackerserve.util.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器
 * 用于验证 JWT Token 并设置当前用户信息到 ThreadLocal
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;
    private final UserService userService;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            String requestPath = request.getRequestURI();
            String requestMethod = request.getMethod();
            
            // 处理 CORS 预检请求（OPTIONS）
            if ("OPTIONS".equals(requestMethod)) {
                System.out.println("拦截器：处理 OPTIONS 预检请求，直接放行");
                return true;
            }
            
            // 排除短链接跳转接口（如 /1, /a 等，但不包括 /api/shortLink/create）
            if (requestPath.matches("^/[^/]+$") && !requestPath.startsWith("/api")) {
                System.out.println("拦截器：短链接跳转接口，直接放行：" + requestPath);
                return true;
            }
            
            // 1. 从请求头获取 Token
            // 支持两种格式：
            // - Authorization: Bearer <token>
            // - token: <token>
            String token = getTokenFromRequest(request);
            
            log.info("=== 拦截器开始处理请求 ===");
            log.info("请求路径: {} {}, Token: {}", requestMethod, requestPath, token != null ? (token.length() > 20 ? token.substring(0, 20) + "..." : token) : "null");
            
            // 2. 如果没有 Token，优先判断是否为“游客模式”访问
            if (!StringUtils.hasText(token)) {
                String guestHeader = request.getHeader("Guest-Access");
                if ("true".equalsIgnoreCase(guestHeader)) {
                    // 游客模式：构造一个虚拟用户对象放入 ThreadLocal
                    User guest = new User();
                    guest.setId(0L);
                    guest.setUsername("GUEST");
                    guest.setNickname("游客");
                    guest.setEmail("guest@local");
                    UserHolder.saveUser(guest);

                    log.info("检测到 Guest-Access 头，当前请求以游客模式访问: {} {}", requestMethod, requestPath);
                    return true;
                }

                System.out.println("未找到 Token，且未启用游客模式");
                log.info("未找到 Token，且未启用游客模式");
                UserHolder.removeUser();

                // 对短链接管理相关接口（/api/shortLink/**）强制要求登录
                if (requestPath.startsWith("/api/shortLink")) {
                    System.out.println("短链接管理接口未登录访问，返回 401");
                    log.warn("未登录访问受保护接口: {} {}", requestMethod, requestPath);
                    writeUnauthorized(response, "未登录或登录已过期，请先登录");
                    return false;
                }

                // 其他接口仍然放行，由各自业务决定是否需要登录
                return true;
            }

            // 3. 验证 Token
            boolean isValid = false;
            try {
                isValid = jwtUtils.validateToken(token);
                System.out.println("Token 验证结果：" + isValid);
                log.info("Token 验证结果: {}", isValid);
            } catch (Exception e) {
                System.out.println("Token 验证异常：" + e.getMessage());
                log.error("Token 验证异常", e);
                UserHolder.removeUser();
                return true;
            }
            
            if (!isValid) {
                System.out.println("Token 验证失败");
                log.warn("Token 验证失败，可能原因：已过期、签名错误或格式不正确");
                UserHolder.removeUser();

                if (requestPath.startsWith("/api/shortLink")) {
                    System.out.println("短链接管理接口使用无效 Token 访问，返回 401");
                    log.warn("无效 Token 访问受保护接口: {} {}", requestMethod, requestPath);
                    writeUnauthorized(response, "登录状态已失效，请重新登录");
                    return false;
                }

                return true;
            }

            // 4. 从 Token 中获取用户 ID
            Long userId = null;
            try {
                userId = jwtUtils.getUserIdFromToken(token);
                System.out.println("从 Token 解析出的 userId: " + userId);
                log.info("从 Token 解析出的 userId: {}", userId);
            } catch (Exception e) {
                System.out.println("解析 userId 异常：" + e.getMessage());
                log.error("解析 userId 异常", e);
                UserHolder.removeUser();
                return true;
            }
            
            if (userId == null) {
                System.out.println("无法从 Token 中解析 userId");
                log.warn("无法从 Token 中解析 userId，Token 可能已损坏");
                UserHolder.removeUser();
                if (requestPath.startsWith("/api/shortLink")) {
                    writeUnauthorized(response, "登录状态异常，请重新登录");
                    return false;
                }
                return true;
            }

            // 5. 查询用户信息并设置到 ThreadLocal
            User user = null;
            try {
                user = userService.findById(userId);
                System.out.println("查询到的用户：" + (user != null ? user.getUsername() : "null"));
            } catch (Exception e) {
                System.out.println("查询用户异常：" + e.getMessage());
                log.error("查询用户异常", e);
            }
            
            if (user != null) {
                UserHolder.saveUser(user);
                System.out.println("✅ 用户信息已设置到 ThreadLocal：userId=" + userId + ", username=" + user.getUsername());
                log.info("✅ 用户信息已成功设置到 ThreadLocal：userId={}, username={}, email={}", 
                        userId, user.getUsername(), user.getEmail());
            } else {
                System.out.println("❌ 用户不存在：userId=" + userId);
                log.warn("❌ 用户不存在：userId={}，无法设置到 ThreadLocal", userId);
                UserHolder.removeUser();
                if (requestPath.startsWith("/api/shortLink")) {
                    writeUnauthorized(response, "用户不存在或已被禁用，请重新登录");
                    return false;
                }
            }

            System.out.println("========================================");
            log.info("=== 拦截器处理完成 ===\n");
            return true;
        } catch (Exception e) {
            System.out.println("拦截器执行异常：" + e.getMessage());
            e.printStackTrace();
            log.error("拦截器执行异常", e);
            UserHolder.removeUser();
            return true;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求结束后清理 ThreadLocal，防止内存泄漏
        UserHolder.removeUser();
    }

    /**
     * 从请求中获取 Token
     * 支持两种方式：
     * 1. Authorization: Bearer <token>
     * 2. token: <token> (请求头或请求参数)
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        // 方式1：从 Authorization 头获取
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            System.out.println("Authorization 请求头: " + (authHeader.length() > 50 ? authHeader.substring(0, 50) + "..." : authHeader));
            String token = authHeader.substring(7);
            System.out.println("从 Authorization 头提取的 Token: " + (token.length() > 30 ? token.substring(0, 30) + "..." : token));
            return token;
        } else {
            System.out.println("Authorization 请求头: null 或格式不符合 Bearer");
        }

        // 方式2：从 token 请求头获取
        String tokenHeader = request.getHeader("token");
        if (StringUtils.hasText(tokenHeader)) {
            System.out.println("从 token 请求头获取: " + tokenHeader.substring(0, Math.min(30, tokenHeader.length())) + "...");
            return tokenHeader;
        }

        // 方式3：从请求参数获取（不推荐，但兼容某些场景）
        String tokenParam = request.getParameter("token");
        if (StringUtils.hasText(tokenParam)) {
            System.out.println("从请求参数获取: " + tokenParam.substring(0, Math.min(30, tokenParam.length())) + "...");
            return tokenParam;
        }

        System.out.println("未找到任何 Token");
        return null;
    }

    /**
     * 向客户端返回 401 未授权响应，前端会根据 HTTP Status=401 进行统一跳转
     */
    private void writeUnauthorized(HttpServletResponse response, String message) {
        try {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            String body = String.format("{\"code\":401,\"message\":\"%s\",\"data\":null}", message);
            response.getWriter().write(body);
        } catch (Exception e) {
            log.error("写入未授权响应失败", e);
        }
    }
}

