package com.example.blog_app_springboot.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.example.blog_app_springboot.common.constants.AppConstants;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    @Profile("!prod")
    public CacheManager devCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                AppConstants.CACHE_TAGS,
                AppConstants.CACHE_USER_PROFILES,
                AppConstants.CACHE_ARTICLE_BY_SLUG,
                AppConstants.CACHE_POPULAR_ARTICLES);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(500));
        return cacheManager;
    }

    @Bean
    @Profile("prod")
    public CacheManager prodCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                AppConstants.CACHE_TAGS,
                AppConstants.CACHE_USER_PROFILES,
                AppConstants.CACHE_ARTICLE_BY_SLUG,
                AppConstants.CACHE_POPULAR_ARTICLES);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .maximumSize(1000));
        return cacheManager;
    }
}
