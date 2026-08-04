package com.focusforge.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;

@Component
@Profile("prod")
@ConditionalOnProperty(name = "app.keep-alive.enabled", havingValue = "true")
public class KeepAliveScheduler {

    private static final Logger log = LoggerFactory.getLogger(KeepAliveScheduler.class);

    private static final int PING_INTERVAL_MS = 300_000; // 5 minutes

    private final RestTemplate restTemplate;
    private final String healthUrl;

    public KeepAliveScheduler(@Value("${app.self-url}") String selfUrl,
                              @Value("${server.servlet.context-path:}") String contextPath,
                              RestTemplateBuilder restTemplateBuilder) {
        // Bound timeouts so a hung or slow ping never ties up a scheduler thread
        // for an unbounded amount of time.
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
        String base = selfUrl.replaceAll("/+$", "");
        this.healthUrl = base + contextPath + "/health";
    }

    // Render free tier spins the instance down after 15 minutes of inactivity;
    // pinging every 5 minutes keeps it warm. This only exists in the "prod"
    // profile AND when explicitly enabled, so local dev and tests never fire
    // network calls at themselves. Everything is wrapped in try/catch because a
    // transient network failure is expected occasionally (e.g. during deploys)
    // and must never bubble up and kill the scheduled task or the app.
    @Scheduled(fixedRate = PING_INTERVAL_MS)
    public void pingSelf() {
        try {
            var response = restTemplate.getForEntity(healthUrl, String.class);
            log.info("Keep-alive ping to {} succeeded (HTTP {}) at {}",
                    healthUrl, response.getStatusCode().value(), Instant.now());
        } catch (Exception ex) {
            log.warn("Keep-alive ping to {} failed at {}: {}",
                    healthUrl, Instant.now(), ex.getMessage());
        }
    }
}
