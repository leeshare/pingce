package com.shaanxi.zhiping.security;

import com.shaanxi.zhiping.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 */
@Slf4j
@Component
public class JwtUtils {

    @Resource
    private JwtConfig jwtConfig;

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 token（小程序用户）
     */
    public String generateToken(Long userId, String openid, String nickname) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("openid", openid);
        claims.put("nickname", nickname != null ? nickname : "");
        claims.put("userType", "wx");
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtConfig.getExpiration());
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 生成 token（管理后台用户）
     */
    public String generateAdminToken(Long adminId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("adminId", adminId);
        claims.put("username", username != null ? username : "");
        claims.put("userType", "admin");
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtConfig.getExpiration());
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析 token
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.warn("token 解析失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 校验 token 是否有效
     */
    public boolean validateToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return false;
        }
        return claims.getExpiration().after(new Date());
    }

    /**
     * 从 token 中获取 userId
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        Object userId = claims.get("userId");
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        }
        return (Long) userId;
    }

    /**
     * 从 token 中获取 openid
     */
    public String getOpenidFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        return (String) claims.get("openid");
    }

    /**
     * 判断 token 是否为管理员 token
     */
    public boolean isAdminToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return false;
        }
        return "admin".equals(claims.get("userType"));
    }

    /**
     * 从管理员 token 中获取 adminId
     */
    public Long getAdminIdFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        Object adminId = claims.get("adminId");
        if (adminId == null) {
            return null;
        }
        if (adminId instanceof Integer) {
            return ((Integer) adminId).longValue();
        }
        if (adminId instanceof Long) {
            return (Long) adminId;
        }
        try {
            return Long.parseLong(String.valueOf(adminId));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 从管理员 token 中获取 username
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        return (String) claims.get("username");
    }
}
