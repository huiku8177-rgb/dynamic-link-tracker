package com.neozeng.trackerserve.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 点击量趋势数据点
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "点击量趋势数据项")
public class ClickTrendItem {
    @Schema(description = "日期，格式：YYYY-MM-DD", example = "2026-01-01")
    private String date;  // 日期，格式：YYYY-MM-DD
    
    @Schema(description = "点击量", example = "100")
    private Long clicks;  // 点击量
}

