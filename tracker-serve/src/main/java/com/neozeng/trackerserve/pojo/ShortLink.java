package com.neozeng.trackerserve.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;

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
public class ShortLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;              // 自增主键
    private String longUrl;       // 对应前端 linkForm.longUrl
    private String shortCode;     // 由 Base62Utils 生成的短码
    private String workspace;     // 对应前端 linkForm.workspace
    private Integer totalClicks;  // 点击统计
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime; // 对应前端 linkForm.expireDate
    private LocalDateTime createTime;
}
