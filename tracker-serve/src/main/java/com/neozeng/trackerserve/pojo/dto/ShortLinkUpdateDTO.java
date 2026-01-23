package com.neozeng.trackerserve.pojo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;

/**
 * @author strive_qin
 * @version 1.0
 * @description ShortLinkUpdateDTO
 * @date 2026/1/13 17:24
 */
@Data // 使用 Lombok 简化代码
@Schema(description = "短链接更新请求DTO")
public class ShortLinkUpdateDTO {

    @NotBlank(message = "原始链接不能为空") // 自动校验非空
    @URL(message = "链接格式不正确")      // 自动校验是否是合法 URL
    @Schema(description = "原始链接", example = "https://www.example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String longUrl;

    // 如果以后需要支持修改有效期，直接在这里加字段即可
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "过期时间", example = "2026-12-31 23:59:59", requiredMode = Schema.RequiredMode.REQUIRED)
     private LocalDateTime expireTime;
}