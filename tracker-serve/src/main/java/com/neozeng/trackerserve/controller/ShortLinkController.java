package com.neozeng.trackerserve.controller;

import com.neozeng.trackerserve.common.Result;
import com.neozeng.trackerserve.pojo.ShortLink;
import com.neozeng.trackerserve.service.ShortLinkService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author strive_qin
 * @version 1.0
 * @description ShortLinkController
 * @date 2026/1/8 14:18
 */
@RestController
@Slf4j
@RequestMapping("/api/shortLink")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ShortLinkController {
    private final ShortLinkService shortLinkService;

    /**
     * 创建短链接
     * @param payload
     * @return
     */
    @PostMapping("/create")
    public Result<String> create(@RequestBody Map<String, String> payload) {
        try {
            String longUrl = payload.get("longUrl");
            String workspace = payload.get("workspace");
            String expireDate = payload.get("expireDate");
            // 调用 Service 生成并存入数据库
            String shortCode = shortLinkService.createShortLink(longUrl, workspace,expireDate);

            return Result.success("https://t.neozeng.com/" + shortCode);
        } catch (Exception e) {
            return Result.error("系统繁忙，请稍后再试");
        }
    }

    /**
     * 展示短链接列表
     */
    @GetMapping("/list")
    public Result<List<ShortLink>> list() {
        return Result.success(shortLinkService.listShortLinks());
    }
    /**
     * 删除短链接
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        shortLinkService.deleteShortLink(id);
        return Result.success(null);
    }


    /**
     * 短链接重定向
     */
    /**
     * 短链接重定向
     * 访问路径：GET /api/shortLink/{shortCode}
     */
    @GetMapping("/{shortCode}")
    public void redirect(@PathVariable String shortCode, HttpServletResponse response) {
        try {
            // 1. 从数据库/缓存获取原始链接
            String longUrl = shortLinkService.getRedirectUrl(shortCode);

            if (longUrl != null) {
                // 2. 更新点击量（异步更好，这里先简单实现）
                shortLinkService.incrementClicks(shortCode);

                // 3. 执行 302 重定向
                response.sendRedirect(longUrl);
            } else {
                // 4. 找不到短码，可以重定向到你的 404 页面
                response.sendRedirect("https://your-domain.com/404");
            }
        } catch (IOException e) {
            log.error("重定向异常", e);
        }
    }



}
