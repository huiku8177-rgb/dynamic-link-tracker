package com.neozeng.trackerserve.mapper;


import com.neozeng.trackerserve.pojo.VisitLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VisitLogMapper extends JpaRepository<VisitLog, Long> {
    Page<VisitLog> findAll(Pageable pageable);
    
    /**
     * 查询指定时间范围内的访问记录
     */
    @Query("SELECT v FROM VisitLog v WHERE v.createTime >= :startTime ORDER BY v.createTime DESC")
    List<VisitLog> findByCreateTimeAfter(@Param("startTime") LocalDateTime startTime);
}
