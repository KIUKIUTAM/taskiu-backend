package com.tavinki.taskiu.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.tavinki.taskiu.enums.RoleType;

@Service
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    public String generateToken(String userId, String name, String email, String avatar, RoleType role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("name", name);
        claims.put("email", email);
        claims.put("avatar", avatar);
        return createToken(claims, userId);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims) // 設定自定義聲明
                .subject(subject) // 設定主體 (通常是用戶名或 ID)
                .issuedAt(new Date(System.currentTimeMillis())) // 發行時間
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 過期時間 (這裡設為 10 小時)
                .signWith(getSigningKey()) // 簽名
                .compact();
    }

    // 3. 驗證 Token 是否有效
    public boolean isTokenValid(String token, int userId) {
        final String extractedUserId = extractUserId(token);
        return (extractedUserId.equals(String.valueOf(userId)) && !isTokenExpired(token));
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

    // 從 Token 中提取過期時間
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
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
                .verifyWith(getSigningKey()) // 使用密鑰驗證簽名
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // 檢查 Token 是否過期
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // 獲取簽名密鑰物件
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
