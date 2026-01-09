package com.neozeng.trackerserve.mapper;

import com.neozeng.trackerserve.pojo.ShortLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author strive_qin
 * @version 1.0
 * @description ShortLinkMapper
 * @date 2026/1/8 14:38
 */
@Repository
public interface ShortLinkMapper extends JpaRepository<ShortLink, Long> {
    ShortLink findByShortCode(String shortCode);

}
