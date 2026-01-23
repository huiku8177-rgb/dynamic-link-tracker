package com.neozeng.trackerserve.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统配置实体类
 * 用于存储全局配置参数，如基础域名、默认过期时长等
 */
@Entity
@Table(name = "t_system_config")
@Data
@Schema(description = "系统配置实体")
public class SystemConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "配置ID", example = "1")
    private Long id;
    /**
     * 配置项键名（唯一）
     * 例如：base_domain, default_expire_days
     */
    @Column(nullable = false, unique = true, length = 100)
    @Schema(description = "配置项键名（唯一）", example = "base_domain")
    private String configKey;
    /**
     * 配置项值
     */
    @Column(nullable = false, length = 500)
    @Schema(description = "配置项值", example = "http://localhost:8080")
    private String configValue;
    /**
     * 配置项描述
     */
    @Column(length = 200)
    @Schema(description = "配置项描述", example = "基础域名")
    private String description;
    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2026-01-01 10:00:00")
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2026-01-01 10:00:00")
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}

