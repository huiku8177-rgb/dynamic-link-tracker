package com.neozeng.trackerserve.service;

import java.util.Map;

/**
 * 系统配置服务接口
 */
public interface SystemConfigService {
    
    /**
     * 获取配置值
     * @param key 配置键
     * @return 配置值，如果不存在返回 null
     */
    String getConfig(String key);
    
    /**
     * 获取配置值（带默认值）
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值，如果不存在返回默认值
     */
    String getConfig(String key, String defaultValue);
    
    /**
     * 设置配置值
     * @param key 配置键
     * @param value 配置值
     * @param description 配置描述
     */
    void setConfig(String key, String value, String description);
    
    /**
     * 批量更新配置
     * @param configs 配置映射
     */
    void batchUpdateConfigs(Map<String, String> configs);
    
    /**
     * 获取所有配置
     * @return 所有配置列表
     */
    Map<String, String> getAllConfigs();
}

