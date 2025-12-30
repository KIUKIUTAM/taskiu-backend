package com.tavinki.taskiu.modules.auth.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.tavinki.taskiu.common.properties.AppTokenProperties;
import com.tavinki.taskiu.modules.auth.entity.RefreshToken;
import com.tavinki.taskiu.modules.auth.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final AppTokenProperties appTokenProperties;

    public RefreshTokenResult processRefreshToken(String refreshToken, String ipAddress, String userAgent) {

        Optional<RefreshToken> tokenRecord = refreshTokenRepository.findByToken(refreshToken);

        // token not found
        if (tokenRecord.isEmpty()) {
            return new RefreshTokenResult(null, false, false, null);
        }

        Instant now = Instant.now();
        Instant expiresAt = tokenRecord.get().getExpiresAt();

        // token valid
        if (expiresAt.isAfter(now)) {
            return new RefreshTokenResult(refreshToken, false, true, tokenRecord.get().getUserId());
        }

        // update token if it's more than half expired
        Instant threshold = now.minusMillis(appTokenProperties.getRefreshTokenDurationMs() / 2);
        if (expiresAt.isAfter(threshold)) {
            String newToken = generateRefreshToken(tokenRecord.get().getUserId(), ipAddress, userAgent);

            return new RefreshTokenResult(newToken, true, true, tokenRecord.get().getUserId());
        }

        // token expired
        return new RefreshTokenResult(null, false, false, null);
    }

    public String generateRefreshToken(String userId, String ipAddress, String userAgent) {
        String tokenString = UUID.randomUUID().toString();

        Instant expiryDate = Instant.now().plusMillis(appTokenProperties.getRefreshTokenDurationMs());

        saveRefreshToken(userId, tokenString, expiryDate, ipAddress, userAgent);
        return tokenString;
    }

    public record RefreshTokenResult(
            String token,
            boolean isRotated,
            boolean isValid,
            String userId) {
    }

    public void saveRefreshToken(String userId, String tokenString, Instant expiryDate, String ipAddress,
            String userAgent) {
        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .token(tokenString)
                .expiresAt(expiryDate)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
        refreshTokenRepository.save(refreshToken);
    }

    public void deleteRefreshToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }
}
