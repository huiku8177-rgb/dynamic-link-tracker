package com.neozeng.trackerserve.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author strive_qin
 * @version 1.0
 * @description Result
 * @date 2026/1/8 14:32
 */
@Data
@Schema(description = "统一响应结果")
public class Result<T> {
    @Schema(description = "状态码：200 成功，500 失败", example = "200")
    private Integer code;    // 状态码：200 成功，500 失败
    
    @Schema(description = "提示信息", example = "操作成功")
    private String message;  // 提示信息
    
    @Schema(description = "实际载荷（短链接对象、列表、字符串等）")
    private T data;          // 实际载荷（短链接对象、列表、字符串等）

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }
}
