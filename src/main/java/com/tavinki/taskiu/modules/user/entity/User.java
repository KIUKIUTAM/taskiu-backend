package com.tavinki.taskiu.modules.user.entity;

import jakarta.persistence.*; // 使用 Jakarta Persistence API
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.time.Instant;

import com.tavinki.taskiu.common.enums.RoleType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String email;

    private String name;

    private String picture;

    private String password;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Auth auth;

    @Enumerated(EnumType.STRING)
    private RoleType role;

    @Builder.Default
    @Column(nullable = false)
    private boolean banned = false;

    @Builder.Default
    @Column(nullable = false)
    private boolean verified = false;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    // --- 內部類保持為 POJO (Plain Old Java Object) 即可 ---
    // 建議加上 Serializable 接口

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Auth implements Serializable {
        private GoogleAuth google;
        private GithubAuth github;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GoogleAuth implements Serializable {
        private String id;
        private String email;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GithubAuth implements Serializable {
        private String id;
        private String email;
    }
}
