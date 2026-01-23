package com.neozeng.trackerserve.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 登录结果 DTO
 */
@Data
@Schema(description = "登录结果响应DTO")
public class LoginResultDTO {
    @Schema(description = "JWT Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;        // JWT Token
    
    @Schema(description = "用户ID", example = "1")
    private Long userId;         // 用户 ID
    
    @Schema(description = "用户名", example = "admin")
    private String username;     // 用户名
    
    @Schema(description = "昵称", example = "管理员")
    private String nickname;     // 昵称
    
    @Schema(description = "邮箱", example = "admin@example.com")
    private String email;        // 邮箱

    public LoginResultDTO(String token, Long userId, String username, String nickname, String email) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
        this.email = email;
    }
}

