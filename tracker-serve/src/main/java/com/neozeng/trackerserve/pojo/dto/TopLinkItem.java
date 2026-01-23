package com.neozeng.trackerserve.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 热门链接排行项
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "热门链接排行项")
public class TopLinkItem {
    @Schema(description = "短链接码", example = "abc123")
    private String shortCode;
    
    @Schema(description = "原始链接", example = "https://www.example.com")
    private String longUrl;
    
    @Schema(description = "总点击量", example = "1000")
    private Integer totalClicks;
}

