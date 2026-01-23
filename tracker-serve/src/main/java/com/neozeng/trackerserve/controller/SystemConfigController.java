package com.neozeng.trackerserve.controller;

import com.neozeng.trackerserve.common.Result;
import com.neozeng.trackerserve.service.SystemConfigService;
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
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 系统配置控制器
 */
@RestController
@Slf4j
@RequestMapping("/api/config")
@RequiredArgsConstructor
@Tag(name = "系统配置管理", description = "系统配置的查询和更新接口")
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    /**
     * 获取所有系统配置
     * GET /api/config
     */
    @Operation(
            summary = "获取所有系统配置",
            description = "获取所有系统配置项，需要用户登录认证",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "401", description = "未授权，需要登录")
    })
    @GetMapping
    public Result<Map<String, String>> getAllConfigs() {
        Map<String, String> configs = systemConfigService.getAllConfigs();
        
        // 如果配置为空，返回默认值
        if (configs.isEmpty()) {
            configs.put("base_domain", "http://localhost:8080");
            configs.put("default_expire_days", "7");
        }
        
        return Result.success(configs);
    }

    /**
     * 批量更新系统配置
     * POST /api/config
     */
    @Operation(
            summary = "批量更新系统配置",
            description = "批量更新系统配置项，需要用户登录认证",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "401", description = "未授权，需要登录"),
            @ApiResponse(responseCode = "500", description = "更新失败")
    })
    @PostMapping
    public Result<Void> updateConfigs(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "配置项键值对映射",
                    required = true,
                    content = @Content(schema = @Schema(example = "{\"base_domain\":\"http://localhost:8080\",\"default_expire_days\":\"7\"}"))
            )
            @RequestBody Map<String, String> configs) {
        try {
            systemConfigService.batchUpdateConfigs(configs);
            return Result.success(null);
        } catch (Exception e) {
            log.error("更新配置失败", e);
            return Result.error("更新配置失败：" + e.getMessage());
        }
    }

    /**
     * 获取单个配置项
     * GET /api/config/{key}
     */
    @Operation(
            summary = "获取单个配置项",
            description = "根据配置键获取配置值，需要用户登录认证",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "401", description = "未授权，需要登录"),
            @ApiResponse(responseCode = "404", description = "配置项不存在")
    })
    @GetMapping("/{key}")
    public Result<String> getConfig(
            @Parameter(description = "配置键", required = true, example = "base_domain")
            @PathVariable String key) {
        String value = systemConfigService.getConfig(key);
        if (value == null) {
            return Result.error("配置项不存在");
        }
        return Result.success(value);
    }
}

