package com.neozeng.trackerserve.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * IP 地址工具类
 * 用于获取真实的客户端 IP 地址（支持代理、负载均衡等场景）
 */
@Slf4j
public class IpUtils {

    private static final String UNKNOWN = "unknown";

    /**
     * 获取客户端真实 IP 地址
     * 支持以下场景：
     * 1. 直接访问
     * 2. 通过代理访问（如 Nginx）
     * 3. 通过负载均衡访问
     * 4. CDN 加速场景
     *
     * @param request HTTP 请求对象
     * @return 客户端真实 IP 地址
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }

        String ip = null;

        // 1. 尝试从 X-Forwarded-For 获取（经过代理时会记录真实 IP）
        ip = request.getHeader("X-Forwarded-For");
        if (isValidIp(ip)) {
            // X-Forwarded-For 可能包含多个 IP，格式：clientIP, proxy1, proxy2
            // 取第一个非 unknown 的 IP
            String[] ips = ip.split(",");
            for (String ipItem : ips) {
                ipItem = ipItem.trim();
                if (isValidIp(ipItem)) {
                    ip = ipItem;
                    break;
                }
            }
        }

        // 2. 尝试从 Proxy-Client-IP 获取
        if (!isValidIp(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }

        // 3. 尝试从 WL-Proxy-Client-IP 获取（WebLogic）
        if (!isValidIp(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }

        // 4. 尝试从 HTTP_CLIENT_IP 获取
        if (!isValidIp(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }

        // 5. 尝试从 HTTP_X_FORWARDED_FOR 获取
        if (!isValidIp(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }

        // 6. 尝试从 X-Real-IP 获取（Nginx 代理）
        if (!isValidIp(ip)) {
            ip = request.getHeader("X-Real-IP");
        }

        // 7. 最后从 RemoteAddr 获取
        if (!isValidIp(ip)) {
            ip = request.getRemoteAddr();
        }

        // 8. 处理 IPv6 本地地址
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            ip = "127.0.0.1";
        }

        log.debug("获取到客户端 IP: {}", ip);
        return ip;
    }

    /**
     * 验证 IP 地址是否有效
     *
     * @param ip IP 地址
     * @return 是否有效
     */
    private static boolean isValidIp(String ip) {
        return ip != null 
                && !ip.isEmpty() 
                && !UNKNOWN.equalsIgnoreCase(ip);
    }

    /**
     * 检查是否为内网 IP
     *
     * @param ip IP 地址
     * @return 是否为内网 IP
     */
    public static boolean isInternalIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }

        // 本地回环地址
        if (ip.startsWith("127.") || ip.equals("localhost")) {
            return true;
        }

        // 私有地址段
        // 10.0.0.0 ~ 10.255.255.255
        // 172.16.0.0 ~ 172.31.255.255
        // 192.168.0.0 ~ 192.168.255.255
        String[] segments = ip.split("\\.");
        if (segments.length == 4) {
            try {
                int first = Integer.parseInt(segments[0]);
                int second = Integer.parseInt(segments[1]);

                if (first == 10) {
                    return true;
                }
                if (first == 172 && second >= 16 && second <= 31) {
                    return true;
                }
                if (first == 192 && second == 168) {
                    return true;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }

        return false;
    }
}

