package com.neozeng.trackerserve.controller;

import com.neozeng.trackerserve.common.Result;
import com.neozeng.trackerserve.service.ShortLinkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
