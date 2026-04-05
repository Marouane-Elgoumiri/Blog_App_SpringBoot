package com.example.blog_app_springboot.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import jakarta.annotation.PostConstruct;

@Configuration
@Getter
public class AppProperties {

    private final Environment environment;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.access-token-expiration-ms}")
    private Long accessTokenExpirationMs;

    @Value("${app.jwt.refresh-token-expiration-ms}")
    private Long refreshTokenExpirationMs;

    @Value("${app.cors.allowed-origins}")
    private String corsAllowedOrigins;

    @Value("${app.rate-limit.authenticated-requests-per-minute}")
    private Integer authenticatedRequestsPerMinute;

    @Value("${app.rate-limit.anonymous-requests-per-minute}")
    private Integer anonymousRequestsPerMinute;

    public AppProperties(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void validate() {
        String activeProfile = environment.getProperty("spring.profiles.active", "dev");

        if ("prod".equals(activeProfile)) {
            validateRequired("DB_URL");
            validateRequired("DB_USERNAME");
            validateRequired("DB_PASSWORD");
            validateRequired("JWT_SECRET");
            validateJwtSecretLength();
        }
    }

    private void validateRequired(String propertyName) {
        String value = environment.getProperty(propertyName);
        if (value == null || value.isBlank() || value.contains("your_") || value.contains("your-")) {
            throw new IllegalStateException(
                    "Missing required configuration for profile 'prod': " + propertyName +
                    ". Please set this environment variable before starting the application.");
        }
    }

    private void validateJwtSecretLength() {
        if (jwtSecret.length() < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET must be at least 32 characters (256 bits) for HS256 algorithm. " +
                    "Current length: " + jwtSecret.length() + " characters.");
        }
    }
}
