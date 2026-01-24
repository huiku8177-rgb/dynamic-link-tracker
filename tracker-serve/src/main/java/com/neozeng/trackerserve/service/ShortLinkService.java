package com.neozeng.trackerserve.service;

import com.neozeng.trackerserve.pojo.ShortLink;
import com.neozeng.trackerserve.pojo.dto.TopLinkItem;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author strive_qin
 * @version 1.0
 * @description ShortLinkService
 * @date 2026/1/8 14:34
 */

public interface ShortLinkService {
     String createShortLink(String longUrl, String workspace, String expireDate) ;

    List<ShortLink> listShortLinks();

    void deleteShortLink(Long id);

    String getRedirectUrl(String shortCode);

    void incrementClicks(String shortCode);

    void recordVisitLog(String shortCode, String ip, String userAgent);

    boolean updateShortLink(Long id, String longUrl, LocalDateTime expireDate);

    List<TopLinkItem> getTopLinksRealTime(int limit);

    /**
     * 从全局排行榜获取热门短链接（游客模式使用）
     * @param limit 返回数量
     * @return 热门短链接列表
     */
    List<TopLinkItem> getTopLinksFromGlobalRanking(int limit);
}
