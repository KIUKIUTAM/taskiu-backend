package com.tavinki.taskiu.common.utils;

import com.tavinki.taskiu.common.enums.role.SystemRole;
import com.tavinki.taskiu.common.properties.AppTokenProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
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
import com.tavinki.taskiu.modules.user.dto.UserResponseDto;

@Service
@RequiredArgsConstructor
public class JwtUtils {

    private final AppTokenProperties appTokenProperties;

    public String generateToken(String userId, String name, String email, String picture, SystemRole role,
            boolean isVerified, boolean isBanned) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("name", name);
        claims.put("email", email);
        claims.put("picture", picture);
        claims.put("verified", isVerified);
        claims.put("banned", isBanned);
        return createToken(claims, userId);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 15 * 60))
                .signWith(getSignature(), Jwts.SIG.HS512)
                .compact();
    }

    public Optional<Claims> getClaims(String token) {
        try {
            return Optional.of(extractAllClaims(token));
        } catch (ExpiredJwtException e) {
            return Optional.of(e.getClaims());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public String refreshToken(String token) {
        final Claims claims = extractAllClaims(token);
        return createToken(claims, claims.getSubject());
    }

    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    private Claims extractAllClaims(String token) {
        String cleanToken = sanitizeToken(token);

        return Jwts.parser()
                .verifyWith(getSignature())
                .build()
                .parseSignedClaims(cleanToken)
                .getPayload();
    }

    private String sanitizeToken(String token) {
        if (token == null)
            return "";
        String clean = token.trim();
        if (clean.startsWith("Bearer ")) {
            clean = clean.substring(7).trim();
        }
        clean = clean.replace("\"", "");
        return clean;
    }

    private SecretKey getSignature() {
        byte[] keyBytes = Decoders.BASE64.decode(appTokenProperties.getJwtSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Optional<UserResponseDto> extractUserFromToken(String jwt) {

        Claims claims = getClaims(jwt).orElse(null);
        if (claims == null) {
            return Optional.empty();
        }
        return Optional.of(UserResponseDto.builder()
                .id(claims.getSubject())
                .name((String) claims.get("name"))
                .email((String) claims.get("email"))
                .picture((String) claims.get("picture"))
                .role(SystemRole.valueOf((String) claims.get("role")))
                .banned((Boolean) claims.get("banned"))
                .verified((Boolean) claims.get("verified"))
                .build());
    }
}
