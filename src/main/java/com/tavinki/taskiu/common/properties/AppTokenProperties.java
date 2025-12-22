package com.tavinki.taskiu.common.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.token")
public class AppTokenProperties {

    private long refreshTokenDurationMs;

    private String jwtSecret;
}