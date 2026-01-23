package com.neozeng.trackerserve.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author strive_qin
 * @version 1.0
 * @description VisistLog
 * @date 2026/1/12 16:41
 */
@Entity
@Table(name = "t_visit_log") // 建议加上前缀，与短链接表 t_short_link 保持一致
@Data
@Schema(description = "访问日志实体")
public class VisitLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "访问日志ID", example = "1")
    private Long id;
    
    @Column(name = "user_id")
    @Schema(description = "用户ID", example = "1")
    private Long userId;

    // 关联的短码
    @Column(nullable = false, length = 20)
    @Schema(description = "短链接码", example = "abc123")
    private String shortCode;

    // 访问者的 IP 地址
    @Schema(description = "访问者IP地址", example = "192.168.1.1")
    private String ip;

    // 访问者的地理位置（后续可通过 IP 接口解析，目前先留空）
    @Schema(description = "访问者地理位置", example = "北京市")
    private String location;

    // 访问者的设备信息（User-Agent）
    @Column(columnDefinition = "TEXT")
    @Schema(description = "访问者设备信息（User-Agent）", example = "Mozilla/5.0...")
    private String userAgent;

    // 访问时间
    @Schema(description = "访问时间", example = "2026-01-01 10:00:00")
    private LocalDateTime createTime;

    // 在写入数据库前自动设置当前时间
    @PrePersist
    protected void onCreate() {
        this.createTime = LocalDateTime.now();
    }
}