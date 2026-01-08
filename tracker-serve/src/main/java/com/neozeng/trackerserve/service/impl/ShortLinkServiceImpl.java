package com.neozeng.trackerserve.service.impl;

import com.neozeng.trackerserve.mapper.ShortLinkMapper;
import com.neozeng.trackerserve.pojo.ShortLink;
import com.neozeng.trackerserve.service.ShortLinkService;
import com.neozeng.trackerserve.util.Base62Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author strive_qin
 * @version 1.0
 * @description ShortLinkServiceImpl
 * @date 2026/1/8 14:35
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ShortLinkServiceImpl implements ShortLinkService {
    private final ShortLinkMapper shortLinkMapper;
    /**
     * @param longUrl
     * @param workspace
     * @return
     */
    @Override
    @Transactional
    public String createShortLink(String longUrl, String workspace,String expireDate) {
        log.info("开始生成短链接");
        log.info("expire: {}", expireDate);
        // 创建对象
        ShortLink link = new ShortLink();
        link.setLongUrl(longUrl);
        link.setWorkspace(workspace);
        link.setTotalClicks(0);
        if (expireDate != null && !expireDate.isEmpty()) {
            // 关键：这里的 pattern 必须和前端的 value-format 完全一致
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            link.setExpireTime(LocalDateTime.parse(expireDate, formatter));
        }
        link.setCreateTime(LocalDateTime.now());

        // 保存第一步：JPA 会自动生成 ID 并回填到 link 对象中
        shortLinkMapper.save(link);

        // 2. 将唯一 ID 转换为 Base62 短码
        String shortCode = Base62Utils.encode(link.getId());

        // 3. 更新短码并再次保存
        link.setShortCode(shortCode);
        shortLinkMapper.save(link);

        return shortCode;
    }
}
