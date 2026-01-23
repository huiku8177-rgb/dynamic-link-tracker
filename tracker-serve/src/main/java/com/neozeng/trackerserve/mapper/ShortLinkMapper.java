package com.neozeng.trackerserve.mapper;

import com.neozeng.trackerserve.pojo.ShortLink;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author strive_qin
 * @version 1.0
 * @description ShortLinkMapper
 * @date 2026/1/8 14:38
 */
@Repository
public interface ShortLinkMapper extends JpaRepository<ShortLink, Long> {
    ShortLink findByShortCode(String shortCode);
    
    /**
     * 查询点击量最多的前 N 个短链接
     */
    @Query("SELECT s FROM ShortLink s ORDER BY s.totalClicks DESC")
    List<ShortLink> findTopByOrderByTotalClicksDesc(Pageable pageable);

    @Modifying // 💡 必须加，表示这是一个修改操作
    @Transactional // 💡 必须加，确保更新操作在事务中执行
    @Query("UPDATE ShortLink s SET s.totalClicks = s.totalClicks + :i WHERE s.shortCode = :shortCode")
    int updateTotalClicks(@Param("shortCode") String shortCode, @Param("i") int i);
}
