package com.neozeng.trackerserve.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

/**
 * @author strive_qin
 * @version 1.0
 * @description ShortLink
 * @date 2026/1/8 14:16
 */
@Data
@Entity
@Table(name = "t_short_link")
@DynamicUpdate
@Schema(description = "短链接实体")
public class ShortLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "短链接ID", example = "1")
    private Long id;// 自增主键
    
    @Column(name = "user_id")
    @Schema(description = "用户ID", example = "1")
    private Long userId;
    
    @Column(name = "long_url", columnDefinition = "TEXT")
    @Schema(description = "原始链接", example = "https://www.example.com")
    private String longUrl;       // 对应前端 linkForm.longUrl
    
    @Schema(description = "短链接码", example = "abc123")
    private String shortCode;     // 由 Base62Utils 生成的短码
    
    @Schema(description = "总点击量", example = "100")
    private Integer totalClicks;  // 点击统计
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "过期时间", example = "2026-12-31 23:59:59")
    private LocalDateTime expireTime;// 对应前端 linkForm.expireDate
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间", example = "2026-01-01 10:00:00")
    private LocalDateTime createTime;
}
