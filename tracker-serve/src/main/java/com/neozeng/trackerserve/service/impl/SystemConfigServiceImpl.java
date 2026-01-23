package com.neozeng.trackerserve.service.impl;

import com.neozeng.trackerserve.mapper.SystemConfigMapper;
import com.neozeng.trackerserve.pojo.SystemConfig;
import com.neozeng.trackerserve.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 系统配置服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SystemConfigServiceImpl implements SystemConfigService {

    private final SystemConfigMapper systemConfigMapper;

    @Override
    public String getConfig(String key) {
        return getConfig(key, null);
    }

    @Override
    public String getConfig(String key, String defaultValue) {
        Optional<SystemConfig> config = systemConfigMapper.findByConfigKey(key);
        return config.map(SystemConfig::getConfigValue).orElse(defaultValue);
    }

    @Override
    @Transactional
    public void setConfig(String key, String value, String description) {
        Optional<SystemConfig> existingConfig = systemConfigMapper.findByConfigKey(key);
        
        if (existingConfig.isPresent()) {
            // 更新现有配置
            SystemConfig config = existingConfig.get();
            config.setConfigValue(value);
            if (description != null) {
                config.setDescription(description);
            }
            systemConfigMapper.save(config);
            log.info("配置已更新: {} = {}", key, value);
        } else {
            // 创建新配置
            SystemConfig config = new SystemConfig();
            config.setConfigKey(key);
            config.setConfigValue(value);
            config.setDescription(description);
            systemConfigMapper.save(config);
            log.info("配置已创建: {} = {}", key, value);
        }
    }

    @Override
    @Transactional
    public void batchUpdateConfigs(Map<String, String> configs) {
        configs.forEach((key, value) -> setConfig(key, value, null));
    }

    @Override
    public Map<String, String> getAllConfigs() {
        List<SystemConfig> configList = systemConfigMapper.findAll();
        Map<String, String> result = new HashMap<>();
        configList.forEach(config -> result.put(config.getConfigKey(), config.getConfigValue()));
        return result;
    }
}

