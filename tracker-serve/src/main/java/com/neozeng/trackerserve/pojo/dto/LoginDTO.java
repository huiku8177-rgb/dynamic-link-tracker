package com.neozeng.trackerserve.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户登录 DTO
 * 支持用户名或邮箱登录
 */
@Data
@Schema(description = "用户登录请求DTO")
public class LoginDTO {
    @NotBlank(message = "用户名或邮箱不能为空")
    @Schema(description = "用户名或邮箱", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
    private String usernameOrEmail;  // 可以是用户名或邮箱

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}

