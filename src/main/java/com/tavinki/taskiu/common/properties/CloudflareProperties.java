package com.tavinki.taskiu.common.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "cloudflare.turnstile")
public class CloudflareProperties {

    private String secretKey;

    private String verifyUrl;

}
