package com.example.blog_app_springboot.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    @Profile("!prod")
    public CacheManager devCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "tags", "userProfiles", "articleBySlug", "popularArticles");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(500));
        return cacheManager;
    }

    @Bean
    @Profile("prod")
    public CacheManager prodCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "tags", "userProfiles", "articleBySlug", "popularArticles");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .maximumSize(1000));
        return cacheManager;
    }
}
