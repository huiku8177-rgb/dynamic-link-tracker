package com.neozeng.trackerserve.service;

import org.springframework.stereotype.Service;

/**
 * @author strive_qin
 * @version 1.0
 * @description ShortLinkService
 * @date 2026/1/8 14:34
 */

public interface ShortLinkService {
     String createShortLink(String longUrl, String workspace, String expireDate) ;

}
