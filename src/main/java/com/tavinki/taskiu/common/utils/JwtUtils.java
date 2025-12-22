package com.tavinki.taskiu.common.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.tavinki.taskiu.common.enums.RoleType;
import com.tavinki.taskiu.common.properties.AppTokenProperties;

@Service
@RequiredArgsConstructor
public class JwtUtils {

    private final AppTokenProperties appTokenProperties;

    public String generateToken(String userId, String name, String email, String picture, RoleType role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("name", name);
        claims.put("email", email);
        claims.put("picture", picture);
        return createToken(claims, userId);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims) // 設定自定義聲明
                .subject(subject) // 設定主體 (通常是用戶名或 ID)
                .issuedAt(new Date(System.currentTimeMillis())) // 發行時間
                .expiration(new Date(System.currentTimeMillis() + 1000 * 15 * 60)) // 過期時間 (這裡設為 15 分鐘)
                .signWith(getSigningKey()) // 簽名
                .compact();
    }

    // 3. 驗證 Token 是否有效
    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 進階版：根據需求決定是否要回傳過期的 Claims
    public Optional<Claims> getClaims(String token) {
        try {
            return Optional.of(extractAllClaims(token));
        } catch (ExpiredJwtException e) {
            return Optional.of(e.getClaims());
        } catch (Exception e) {
            // 簽名錯誤或格式錯誤，絕對不能回傳
            return Optional.empty();
        }
    }

    public String refreshToken(String token) {
        final Claims claims = extractAllClaims(token);
        return createToken(claims, claims.getSubject());
    }

    // --- 以下為輔助方法 ---

    // 從 Token 中提取用戶 ID
    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 泛型方法：提取特定的 Claim
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractRole(String token) {
        // 使用泛型方法提取，並指定 Key 為 "role"
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    // 解析 Token 取得所有 Claims
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

    }

    // 獲取簽名密鑰物件
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(appTokenProperties.getJwtSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
