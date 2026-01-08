package com.tavinki.taskiu.modules.email.repository;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class VerificationCodeRepository {

    private final StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "verify:code:";

    /**
     * saving verification code to redis with expiration time
     * 
     * @param email
     * @param code
     * @param minutes
     */
    public void save(@NonNull String email, @NonNull String code, long minutes) {
        String key = KEY_PREFIX + email;
        // set(key, value, timeout)
        redisTemplate.opsForValue().set(key, code, Objects.requireNonNull(Duration.ofMinutes(minutes)));
    }

    /**
     * get verification code by email
     * 
     * @param email user email
     * @return verification code, or null if expired or not found
     */
    public String get(String email) {
        String key = KEY_PREFIX + email;
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * delete verification code (usually manually deleted after successful
     * verification to prevent reuse)
     * 
     * @param email user email
     */
    public void delete(String email) {
        String key = KEY_PREFIX + email;
        redisTemplate.delete(key);
    }

    /**
     * verify if the verification code is correct
     */
    public boolean verify(String email, String inputCode) {
        String storedCode = get(email);
        // check if there is a value in Redis and if it matches the user input
        return storedCode != null && storedCode.equals(inputCode);
    }
}
