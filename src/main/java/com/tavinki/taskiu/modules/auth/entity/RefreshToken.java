package com.tavinki.taskiu.modules.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "refresh_tokens") // 獨立的 Collection
public class RefreshToken {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed(unique = true)
    private String token;

    private String userAgent;
    private String ipAddress;

    @Indexed(expireAfter = "0s")
    private Instant expiresAt;
}