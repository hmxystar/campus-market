package com.campus.market.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类（jjwt 0.12.6 新 API）：
 * 签名 signWith(key, Jwts.SIG.HS256)、解析 verifyWith / parseSignedClaims / getPayload，
 * 与 0.11.5 的 setSigningKey / parseClaimsJws 写法不兼容。
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expire}")
    private long expire;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String create(Long userId, Integer role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))          // 主题 = 用户 id
                .claim("role", role)                      // 自定义声明 = 角色
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expire))
                .signWith(key(), Jwts.SIG.HS256)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
