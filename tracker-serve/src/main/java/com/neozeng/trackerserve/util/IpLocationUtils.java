package com.neozeng.trackerserve.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * IP 地理位置查询工具类
 * 使用免费的 IP 地理位置查询 API
 */
@Slf4j
@Component
public class IpLocationUtils {

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 根据 IP 地址查询地理位置
     * 使用 ip-api.com 免费服务（无需 API Key，但有请求限制：45 次/分钟）
     *
     * @param ip IP 地址
     * @return 地理位置描述，格式：国家-省份-城市，例如："中国-北京-北京"
     */
    public static String getLocation(String ip) {
        // 1. 检查是否为内网 IP
        if (IpUtils.isInternalIp(ip)) {
            return "内网IP";
        }

        // 2. 检查是否为本地回环地址
        if ("127.0.0.1".equals(ip) || "localhost".equals(ip)) {
            return "本地";
        }

        try {
            // 3. 调用免费 IP 查询 API
            // API 文档：https://ip-api.com/docs/api:json
            String url = String.format("http://ip-api.com/json/%s?lang=zh-CN&fields=status,message,country,regionName,city", ip);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode jsonNode = objectMapper.readTree(response.body());
                
                // 检查 API 返回状态
                String status = jsonNode.get("status").asText();
                if ("success".equals(status)) {
                    String country = jsonNode.has("country") ? jsonNode.get("country").asText() : "";
                    String region = jsonNode.has("regionName") ? jsonNode.get("regionName").asText() : "";
                    String city = jsonNode.has("city") ? jsonNode.get("city").asText() : "";

                    // 组装地理位置字符串
                    StringBuilder location = new StringBuilder();
                    if (!country.isEmpty()) {
                        location.append(country);
                    }
                    if (!region.isEmpty()) {
                        if (location.length() > 0) location.append("-");
                        location.append(region);
                    }
                    if (!city.isEmpty() && !city.equals(region)) {
                        if (location.length() > 0) location.append("-");
                        location.append(city);
                    }

                    String result = location.toString();
                    log.debug("IP {} 解析为地理位置: {}", ip, result);
                    return result.isEmpty() ? "未知" : result;
                } else {
                    String message = jsonNode.has("message") ? jsonNode.get("message").asText() : "unknown";
                    log.warn("IP 地理位置查询失败: {}, IP: {}", message, ip);
                }
            }
        } catch (Exception e) {
            log.error("查询 IP 地理位置异常: {}, IP: {}", e.getMessage(), ip);
        }

        return "未知";
    }

    /**
     * 异步查询地理位置（不阻塞主流程）
     * 适用于对实时性要求不高的场景
     *
     * @param ip       IP 地址
     * @param callback 回调函数
     */
    public static void getLocationAsync(String ip, LocationCallback callback) {
        new Thread(() -> {
            String location = getLocation(ip);
            callback.onLocationResolved(location);
        }).start();
    }

    /**
     * 地理位置解析回调接口
     */
    public interface LocationCallback {
        void onLocationResolved(String location);
    }
}

