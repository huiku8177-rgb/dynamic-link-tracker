package com.neozeng.trackerserve.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 前端用来恢复登录态的用户信息 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户基本信息 DTO")
public class UserInfoDTO {

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "昵称", example = "管理员")
    private String nickname;

    @Schema(description = "邮箱", example = "admin@example.com")
    private String email;
}


