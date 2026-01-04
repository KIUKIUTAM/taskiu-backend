package com.tavinki.taskiu.common.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@ConfigurationProperties(prefix = "security.csp")
public class CspProperties {

    private Map<String, List<String>> policy;

    public Map<String, List<String>> getPolicy() {
        return policy;
    }

    public void setPolicy(Map<String, List<String>> policy) {
        this.policy = policy;
    }

    @Override
    public String toString() {
        if (policy == null || policy.isEmpty()) {
            return "default-src 'self'";
        }

        return policy.entrySet().stream()
                .map(entry -> {
                    String directive = entry.getKey();
                    String sources = String.join(" ", entry.getValue());
                    return directive + " " + sources;
                })
                .collect(Collectors.joining("; "));
    }
}
