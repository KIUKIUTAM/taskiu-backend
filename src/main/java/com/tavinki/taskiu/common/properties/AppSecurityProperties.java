package com.tavinki.taskiu.common.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

import java.util.List;
import java.util.ArrayList;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.security")
public class AppSecurityProperties {

    private List<String> whitelist = new ArrayList<>();

}