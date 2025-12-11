package com.tavinki.taskiu.mongo.entity;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.tavinki.taskiu.enums.RoleType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String name;

    private String avatar;

    private String password;

    private Auth auth;

    private RoleType role;

    private boolean isBanned;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Data
    @Builder // 2. 中層加 Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Auth {
        private GoogleAuth google;
    }

    @Data
    @Builder // 2. 中層加 Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GoogleAuth {
        private String id;
        private String email;
    }

}
