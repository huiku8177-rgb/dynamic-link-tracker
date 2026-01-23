package com.neozeng.trackerserve.config;

import com.neozeng.trackerserve.interceptor.LoginInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置类
 * 配置拦截器
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        System.out.println("注册 LoginInterceptor 拦截器");
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")  // 拦截所有路径
                .excludePathPatterns(
                        "/api/auth/**",         // 排除认证相关接口
                        "/error",                  // 排除错误页面
                        "/favicon.ico",            // 排除图标
                        "/swagger-ui/**",          // 排除 Swagger UI
                        "/v3/api-docs/**",         // 排除 OpenAPI 文档
                        "/swagger-ui.html"         // 排除 Swagger UI 页面
                )
                // 注意：不要排除 /**/{shortCode}，因为这种模式可能匹配到 /api/shortLink/create
                // 短链接跳转接口应该通过其他方式处理（如 RedirectController）
                .order(1);  // 设置拦截器执行顺序
    }
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}

