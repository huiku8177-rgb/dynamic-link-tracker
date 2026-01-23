package com.neozeng.trackerserve.mapper;

import com.neozeng.trackerserve.pojo.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemConfigMapper extends JpaRepository<SystemConfig, Long> {
    /**
     * 根据配置键查询配置项
     */
    Optional<SystemConfig> findByConfigKey(String configKey);
}

