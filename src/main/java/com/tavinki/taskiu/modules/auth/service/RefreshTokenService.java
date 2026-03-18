package com.tavinki.taskiu.modules.auth.service;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.ResponseCookie;
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

    /**
     * Process the provided refresh token.
     * 
     * @param refreshToken
     * @param ipAddress
     * @param userAgent
     * @return RefreshTokenResult indicating the status of the token.
     *         RefreshTokenResult contains:
     *         <ul>
     *         <li>token: the new token if rotated, or the same token if still
     *         valid,</li>
     *         <li>isRotated: boolean indicating if the token was rotated,</li>
     *         <li>isValid: boolean indicating if the token is valid,</li>
     *         <li>userId: the user ID associated with the token.</li>
     *         </ul>
     */
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

    /**
     * Record to represent the result of processing a refresh token.
     * 
     * @param token     The new or existing refresh token.
     * @param isRotated Indicates if the token was rotated.
     * @param isValid   Indicates if the token is valid.
     * @param userId    The user ID associated with the token.
     */
    public record RefreshTokenResult(
            String token,
            boolean isRotated,
            boolean isValid,
            String userId) {
    }

    // save to mongodb
    public void saveRefreshToken(String userId, String tokenString, Instant expiryDate, String ipAddress,
            String userAgent) {
        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .token(tokenString)
                .expiresAt(expiryDate)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
        refreshTokenRepository.save(Objects.requireNonNull(refreshToken));
    }

    public void deleteRefreshToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }

    public ResponseCookie generateRefreshTokenCookieForm(String validRefreshToken) {
        return ResponseCookie.from("refreshToken", validRefreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(7L * 24 * 60 * 60)
                .sameSite("Strict")
                .build();
    }

    public ResponseCookie clearRefreshTokenCookieForm() {
        return ResponseCookie.from("refreshToken", "")
                .httpOnly(false)
                .secure(false)
                .path("/")
                .maxAge(0L)
                .sameSite("Lax")
                .build();
    }
}
