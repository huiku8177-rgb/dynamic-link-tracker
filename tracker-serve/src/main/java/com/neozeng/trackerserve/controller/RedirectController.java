package com.neozeng.trackerserve.controller;

import cn.hutool.core.util.StrUtil;
import com.neozeng.trackerserve.service.ShortLinkService;
import com.neozeng.trackerserve.util.CacheClient;
import com.neozeng.trackerserve.util.IpUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;

/**
 * @author strive_qin
 * @version 1.0
 * @description RedirectController
 * @date 2026/1/12 16:47
 */
@Controller
@RequiredArgsConstructor
@Slf4j
@Tag(name = "短链接重定向", description = "短链接访问和重定向接口")
public class RedirectController {
    private final ShortLinkService shortLinkService;
    private final CacheClient cacheClient;
    private final StringRedisTemplate redisTemplate;

    @Operation(
            summary = "短链接重定向",
            description = "根据短码访问并重定向到原始链接，同时记录访问日志和增加点击量"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "重定向成功"),
            @ApiResponse(responseCode = "404", description = "短链接不存在或已过期")
    })
    @GetMapping("/{shortCode}")
    public void redirect(
            @Parameter(description = "短链接码", required = true, example = "abc123")
            @PathVariable String shortCode,
            HttpServletResponse response,
            HttpServletRequest request) {
        try {
            if ("favicon.ico".equals(shortCode)) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            // 1. 调用 Service 获取原始链接（Service 内部使用 CacheClient）
            String longUrl = shortLinkService.getRedirectUrl(shortCode);

            if (StrUtil.isNotBlank(longUrl)) {
                // 2. 异步处理点击量统计和日志记录（不阻塞跳转）
                String ip = IpUtils.getClientIp(request);
                String ua = request.getHeader("User-Agent");

                // 建议在 Service 方法上加 @Async
                shortLinkService.incrementClicks(shortCode);

                shortLinkService.recordVisitLog(shortCode, ip, ua);

                // 3. 执行重定向（normalizeUrl 逻辑建议移至 Service 或工具类）
                response.sendRedirect(normalizeUrl(longUrl));
            } else {
                // 4. 处理找不到短码的情况（CacheClient 已处理缓存穿透，这里直接 404）
                log.warn("短链接 {} 不存在或已过期", shortCode);
                response.sendRedirect("https://your-domain.com/404");
            }
        } catch (IOException e) {
            log.error("重定向异常，shortCode={}", shortCode, e);
        }
    }


    /**
     * 规范化 URL，确保有协议前缀
     * 如果 URL 没有 http:// 或 https://，默认添加 https://
     */
    private String normalizeUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return url;
        }
        url = url.trim();
        // 如果已经有协议前缀，直接返回
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }
        // 如果没有协议前缀，默认添加 https://
        // 特殊处理 chrome://, file:// 等协议
        if (url.contains("://")) {
            return url; // 已有其他协议，保持不变
        }
        return "https://" + url;
    }
}