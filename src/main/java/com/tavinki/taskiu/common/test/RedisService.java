
package com.tavinki.taskiu.common.test;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import org.springframework.lang.NonNull;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    // Save data
    public void save(@NonNull String key, @NonNull Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    // Retrieve data
    public Object get(@NonNull String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // Delete data
    public void delete(@NonNull String key) {
        redisTemplate.delete(key);
    }
}
