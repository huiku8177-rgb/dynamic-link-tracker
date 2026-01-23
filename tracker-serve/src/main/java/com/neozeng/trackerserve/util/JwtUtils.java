package com.neozeng.trackerserve.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * JWT 工具类
 * 用于生成和解析 JWT Token
 */
@Component
public class JwtUtils {

    /**
     * JWT 密钥（从配置文件读取，如果没有则使用默认值）
     * 生产环境建议使用环境变量或配置中心管理
     */
    @Value("${jwt.secret:zeng-tracker-secure-key-ensure-length-is-over-32-bytes-please}")
    private String secret;

    /**
     * Token 过期时间（毫秒），默认 7 天
     */
    @Value("${jwt.expiration:604800000}")
    private Long expiration;

    /**
     * 获取签名密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成 JWT Token
     *
     * @param userId   用户 ID
     * @param username 用户名
     * @return Token 字符串
     */
    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 生成 JWT Token（支持自定义 Claims）
     *
     * @param userId   用户 ID
     * @param username 用户名
     * @param claims   额外的 Claims
     * @return Token 字符串
     */
    public String generateToken(Long userId, String username, Map<String, Object> claims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        var builder = Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .issuedAt(now)
                .expiration(expiryDate);

        if (claims != null && !claims.isEmpty()) {
            builder.claims(claims);
        }

        return builder.signWith(getSigningKey()).compact();
    }

    /**
     * 从 Token 中获取用户 ID
     *
     * @param token JWT Token
     * @return 用户 ID，如果 Token 无效则返回 null
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            String subject = claims.getSubject();
            return subject != null ? Long.parseLong(subject) : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从 Token 中获取用户名
     *
     * @param token JWT Token
     * @return 用户名，如果 Token 无效则返回 null
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.get("username", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 解析 Token 获取 Claims
     *
     * @param token JWT Token
     * @return Claims 对象
     * @throws io.jsonwebtoken.JwtException 如果 Token 无效
     */
    public Claims parseToken(String token) {
        try {
            System.out.println("开始解析 Token，secret 长度：" + (secret != null ? secret.length() : 0));
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims;
        } catch (ExpiredJwtException | SignatureException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 验证 Token 是否有效
     *
     * @param token JWT Token
     * @return 如果 Token 有效返回 true，否则返回 false
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            // 检查是否过期（parseToken 内部已检查，这里做双重保障）
            return claims.getExpiration().after(new Date());
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // Token 已过期

            return false;
        } catch (io.jsonwebtoken.security.SignatureException e) {
            // 签名验证失败（可能是密钥不一致）
            return false;
        } catch (Exception e) {
            // 其他异常（格式错误等）
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 检查 Token 是否过期
     *
     * @param token JWT Token
     * @return 如果过期返回 true，否则返回 false
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}

