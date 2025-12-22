package com.tavinki.taskiu.modules.auth.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.tavinki.taskiu.modules.auth.entity.RefreshToken;

@Repository
public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {
    Optional<RefreshToken> findByToken(String token);

    void deleteByToken(String token);

    void deleteByUserId(String userId);
}
