package se.comerit.avanza.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rate-limit")
public record RateLimitProperties(
        boolean enabled,
        Limit login,
        Limit api) {

    public record Limit(
            long capacity,
            long refillTokens,
            Duration refillPeriod) {
    }
}
