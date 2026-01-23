package com.neozeng.trackerserve.controller;

import com.neozeng.trackerserve.common.Result;
import com.neozeng.trackerserve.mapper.ShortLinkMapper;
import com.neozeng.trackerserve.mapper.VisitLogMapper;
import com.neozeng.trackerserve.pojo.ShortLink;
import com.neozeng.trackerserve.pojo.VisitLog;
import com.neozeng.trackerserve.pojo.dto.ClickTrendItem;
import com.neozeng.trackerserve.pojo.dto.ShortLinkUpdateDTO;
import com.neozeng.trackerserve.pojo.dto.TopLinkItem;
import com.neozeng.trackerserve.service.ShortLinkService;
import com.neozeng.trackerserve.util.UserHolder;
import com.neozeng.trackerserve.pojo.User;
import com.neozeng.trackerserve.exception.UnAuthorizedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author strive_qin
 * @version 1.0
 * @description ShortLinkController
 * @date 2026/1/8 14:18
 */
@RestController
@Slf4j
@RequestMapping("/api/shortLink")
@RequiredArgsConstructor
@Tag(name = "短链接管理", description = "短链接的创建、查询、更新、删除以及统计分析接口")
public class ShortLinkController {
    private final ShortLinkService shortLinkService;
    private final VisitLogMapper visitLogMapper;
    private final ShortLinkMapper shortLinkMapper;

    /**
     * 创建短链接
     * @param payload
     * @return
     */
    @Operation(
            summary = "创建短链接",
            description = "根据原始链接创建短链接，需要用户登录认证",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "创建成功", 
                    content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "401", description = "未授权，需要登录"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    @PostMapping("/create")
    public Result<String> create(
            @Parameter(description = "请求体，包含 longUrl（原始链接）、workspace（工作空间，可选）、expireDate（过期时间，可选）", 
                    required = true, 
                    schema = @Schema(example = "{\"longUrl\":\"https://example.com\",\"workspace\":\"default\",\"expireDate\":\"2026-12-31 23:59:59\"}"))
            @RequestBody Map<String, String> payload, 
            HttpServletRequest request) {
        // 诊断：打印请求头信息
        String authHeader = request.getHeader("Authorization");
        log.info("Controller 接收到的 Authorization 头: {}", authHeader != null ? (authHeader.length() > 50 ? authHeader.substring(0, 50) + "..." : authHeader) : "null");
        System.out.println("[Controller] Authorization 头: " + (authHeader != null ? authHeader.substring(0, Math.min(50, authHeader.length())) + "..." : "null"));
        try {
            String longUrl = payload.get("longUrl");
            String workspace = payload.get("workspace");
            String expireDate = payload.get("expireDate");
            // 调用 Service 生成并存入数据库
            // 调用 Service 生成并存入数据库
            String shortCode = shortLinkService.createShortLink(longUrl, workspace, expireDate);

            // 💡 重点：直接返回 shortCode，不要在后端写死域名
            return Result.success(shortCode);
        } catch (Exception e) {
            return Result.error("系统繁忙，请稍后再试");
        }
    }

    /**
     * 展示短链接列表
     */
    @Operation(
            summary = "获取短链接列表",
            description = "获取当前用户的所有短链接列表，需要用户登录认证",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "401", description = "未授权，需要登录")
    })
    @GetMapping("/list")
    public Result<List<ShortLink>> list() {
        return Result.success(shortLinkService.listShortLinks());
    }
    /**
     * 删除短链接
     */
    @Operation(
            summary = "删除短链接",
            description = "根据ID删除指定的短链接，需要用户登录认证",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "401", description = "未授权，需要登录"),
            @ApiResponse(responseCode = "404", description = "短链接不存在")
    })
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "短链接ID", required = true, example = "1")
            @PathVariable Long id) {
        shortLinkService.deleteShortLink(id);
        return Result.success(null);
    }
    /**
     * 修改原始链接
     * 对应前端地址：PUT /api/shortLink/{id}
     */
    /**
     * 修改原始链接
     */
    @Operation(
            summary = "更新短链接",
            description = "更新指定短链接的原始链接和过期时间，需要用户登录认证",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "400", description = "请求参数错误"),
            @ApiResponse(responseCode = "401", description = "未授权，需要登录"),
            @ApiResponse(responseCode = "404", description = "短链接不存在")
    })
    @PutMapping("/{id}")
    public Result<Void> update(
            @Parameter(description = "短链接ID", required = true, example = "1")
            @PathVariable Long id, 
            @Parameter(description = "更新信息", required = true)
            @RequestBody ShortLinkUpdateDTO updateDto) {
        // 1. 简单的判空校验
        if (updateDto.getLongUrl() == null || updateDto.getLongUrl().isBlank()) {
            return Result.error("原始链接不能为空");
        }
        if (updateDto.getExpireTime() == null) {
            return Result.error("有效期不能为空");
        }

        // 2. 调用 Service 进行更新
        boolean success = shortLinkService.updateShortLink(id, updateDto.getLongUrl(), updateDto.getExpireTime());

        return success ? Result.success(null) : Result.error("记录不存在或更新失败");
    }
    /**
     * 获取控制台所需的最近 5 条访问记录
     * 对应前端地址：GET /api/shortLink/visits/recent
     */
    @Operation(
            summary = "获取最近访问记录",
            description = "获取最近5条短链接访问记录，需要用户登录认证",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "401", description = "未授权，需要登录")
    })
    @GetMapping("/visits/recent")
    public Result<List<VisitLog>> getRecentVisits() {
        User user = UserHolder.getUser();
        if (user == null) {
            throw new UnAuthorizedException();
        }

        // 游客模式下不返回任何真实访问记录，提示需登录
        if (user.getId() != null && user.getId() == 0L) {
            throw new UnAuthorizedException("游客模式下无法查看访问详情，请登录后再试");
        }

        // 当前用户最近 5 条访问记录
        Pageable pageable = PageRequest.of(0, 5, Sort.by("createTime").descending());
        Page<VisitLog> page = visitLogMapper.findAll(pageable);
        List<VisitLog> filtered = page.getContent().stream()
                .filter(v -> user.getId().equals(v.getUserId()))
                .toList();
        return Result.success(filtered);
    }

    /**
     * 获取所有访问记录（分页）
     * 对应前端地址：GET /api/shortLink/visits/all?page=0&size=20
     */
    @Operation(
            summary = "获取所有访问记录（分页）",
            description = "分页获取所有短链接访问记录，需要用户登录认证",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "401", description = "未授权，需要登录")
    })
    @GetMapping("/visits/all")
    public Result<Map<String, Object>> getAllVisits(
            @Parameter(description = "页码，从0开始", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        User user = UserHolder.getUser();
        if (user == null) {
            throw new UnAuthorizedException();
        }

        // 游客模式禁止访问完整访问详情列表
        if (user.getId() != null && user.getId() == 0L) {
            throw new UnAuthorizedException("游客模式下无法查看访问详情列表，请登录后继续");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        Page<VisitLog> visitPage = visitLogMapper.findAll(pageable);

        List<VisitLog> filteredContent = visitPage.getContent().stream()
                .filter(v -> user.getId().equals(v.getUserId()))
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("content", filteredContent);
        response.put("totalElements", (long) filteredContent.size());
        
        return Result.success(response);
    }

    /**
     * 获取点击量趋势（过去 N 天）
     * 对应前端地址：GET /api/shortLink/stats/clickTrend?days=7
     */
    @Operation(
            summary = "获取点击量趋势",
            description = "获取过去N天的点击量趋势数据，需要用户登录认证",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "401", description = "未授权，需要登录")
    })
    @GetMapping("/stats/clickTrend")
    public Result<List<ClickTrendItem>> getClickTrend(
            @Parameter(description = "统计天数，默认7天", example = "7")
            @RequestParam(defaultValue = "7") int days) {
        User user = UserHolder.getUser();
        if (user == null) {
            throw new UnAuthorizedException();
        }

        // 计算开始时间
        LocalDateTime startTime = LocalDateTime.now().minusDays(days).withHour(0).withMinute(0).withSecond(0);
        
        // 查询时间范围内的访问记录
        List<VisitLog> logs = visitLogMapper.findByUserIdAndCreateTimeAfter(user.getId(), startTime);
        
        // 按日期分组统计
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Map<String, Long> dailyClicks = logs.stream()
                .collect(Collectors.groupingBy(
                        log -> log.getCreateTime().format(formatter),
                        Collectors.counting()
                ));
        
        // 生成完整的日期序列（包括没有点击的日期）
        List<ClickTrendItem> trendData = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = currentDate.minusDays(i);
            String dateStr = date.format(formatter);
            Long clicks = dailyClicks.getOrDefault(dateStr, 0L);
            trendData.add(new ClickTrendItem(dateStr, clicks));
        }
        
        return Result.success(trendData);
    }

    /**
     * 获取热门短链接排行
     * 对应前端地址：GET /api/shortLink/stats/topLinks?limit=5
     */
    @Operation(
            summary = "获取热门短链接排行",
            description = "获取点击量最高的短链接排行，需要用户登录认证",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "401", description = "未授权，需要登录")
    })
    @GetMapping("/stats/topLinks")
    public Result<List<TopLinkItem>> getTopLinks(
            @Parameter(description = "返回数量，默认5条", example = "5")
            @RequestParam(defaultValue = "5") int limit) {

        // 💡 调用 Service 层的新逻辑
        List<TopLinkItem> result = shortLinkService.getTopLinksRealTime(limit);

        return Result.success(result);
    }

}
