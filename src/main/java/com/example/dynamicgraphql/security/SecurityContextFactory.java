package com.example.dynamicgraphql.security;

import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class SecurityContextFactory {

    public Map<String, Object> fromHeaders(Map<String, String> headers) {
        return Map.of(
                "environment", headers.getOrDefault("X-Security-Env", "public"),
                "user", headers.getOrDefault("X-User-Id", "anonymous"));
    }
}
