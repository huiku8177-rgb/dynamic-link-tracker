package com.neozeng.trackerserve.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author strive_qin
 * @version 1.0
 * @description User
 * @date 2026/1/18 14:13
 */

    @Entity
    @Table(name = "t_user")
    @Data
    @Schema(description = "用户实体")
    public class User {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Schema(description = "用户ID", example = "1")
        private Long id;

        @Column(nullable = false, unique = true, length = 50)
        @Schema(description = "用户名（唯一）", example = "admin")
        private String username;  // 用户名（唯一）

        @Column(nullable = false, unique = true, length = 100)
        @Schema(description = "邮箱（唯一，可用于登录）", example = "admin@example.com")
        private String email;     // 邮箱（唯一，可用于登录）

        @Column(nullable = false, length = 255)
        @Schema(description = "密码（加密存储）", example = "$2a$10$...")
        private String password;  // 密码（加密存储，如 BCrypt）

        @Schema(description = "昵称（可选）", example = "管理员")
        private String nickname;  // 昵称（可选）

        @Schema(description = "头像 URL（可选）", example = "https://example.com/avatar.jpg")
        private String avatar;    // 头像 URL（可选）
        
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "创建时间", example = "2026-01-01 10:00:00")
        private LocalDateTime createTime;
        
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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
