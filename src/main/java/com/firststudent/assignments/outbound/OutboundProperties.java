package com.firststudent.assignments.outbound;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.outbound")
public record OutboundProperties(
        String baseUrl,
        String path,
        Duration connectTimeout,
        Duration responseTimeout
) {
}
