package com.example.blog_app_springboot.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitService {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final int authenticatedLimit;
    private final int anonymousLimit;

    public RateLimitService(
            @Value("${app.rate-limit.authenticated-requests-per-minute:100}") int authenticatedLimit,
            @Value("${app.rate-limit.anonymous-requests-per-minute:20}") int anonymousLimit) {
        this.authenticatedLimit = authenticatedLimit;
        this.anonymousLimit = anonymousLimit;
    }

    public Bucket resolveBucket(String key, boolean authenticated) {
        return buckets.computeIfAbsent(key, k -> newBucket(authenticated));
    }

    private Bucket newBucket(boolean authenticated) {
        int limit = authenticated ? authenticatedLimit : anonymousLimit;
        Bandwidth bandwidth = Bandwidth.classic(limit, Refill.greedy(limit, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(bandwidth).build();
    }
}
