package com.tavinki.taskiu.common.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "google")
public class GoogleLoginProperties {

    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String tokenUri;

}
