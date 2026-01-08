package com.tavinki.taskiu.common.utils;

import jakarta.servlet.http.HttpServletRequest;

public class HttpUtils {

    private HttpUtils() {
        throw new IllegalStateException("Utility class");
    }

    private static final String[] IP_HEADERS = {
            "CF-Connecting-IP",
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    };

    public static String getRequestIP(HttpServletRequest request) {
        for (String header : IP_HEADERS) {
            String value = request.getHeader(header);
            if (value == null || value.isEmpty() || "unknown".equalsIgnoreCase(value)) {
                continue;
            }
            // X-Forwarded-For 可能包含多個 IP，格式為 "client, proxy1, proxy2"
            // 我們只需要第一個，也就是真實客戶端 IP
            String[] parts = value.split("\\s*,\\s*");
            return parts[0];
        }
        return request.getRemoteAddr();
    }

    public static String getUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return userAgent != null ? userAgent : "Unknown";
    }
}
