package com.neozeng.trackerserve.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户注册 DTO
 */
@Data
@Schema(description = "用户注册请求DTO")
public class RegisterDTO {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在 3-50 个字符之间")
    @Schema(description = "用户名（3-50个字符）", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱地址", example = "admin@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在 6-20 个字符之间")
    @Schema(description = "密码（6-20个字符）", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @Schema(description = "昵称（可选）", example = "管理员")
    private String nickname;  // 昵称（可选）
}

