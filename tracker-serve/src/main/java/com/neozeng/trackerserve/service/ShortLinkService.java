package com.neozeng.trackerserve.service;

import com.neozeng.trackerserve.pojo.ShortLink;
import org.springframework.stereotype.Service;

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
}
