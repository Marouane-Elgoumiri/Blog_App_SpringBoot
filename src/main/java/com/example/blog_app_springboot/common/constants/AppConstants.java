package com.example.blog_app_springboot.common.constants;

public final class AppConstants {

    private AppConstants() {
    }

    public static final String API_BASE_PATH = "/api/v1";

    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;
    public static final String DEFAULT_SORT_FIELD = "createdAt";
    public static final String DEFAULT_SORT_DIRECTION = "desc";

    public static final String CACHE_TAGS = "tags";
    public static final String CACHE_USER_PROFILES = "userProfiles";
    public static final String CACHE_ARTICLE_BY_SLUG = "articleBySlug";
    public static final String CACHE_POPULAR_ARTICLES = "popularArticles";

    public static final int RATE_LIMIT_AUTHENTICATED_PER_MINUTE = 100;
    public static final int RATE_LIMIT_ANONYMOUS_PER_MINUTE = 20;

    public static final long TOKEN_EXPIRATION_ACCESS_MS = 900_000;
    public static final long TOKEN_EXPIRATION_REFRESH_MS = 604_800_000;

    public static final int READING_TIME_WORDS_PER_MINUTE = 200;

    public static final long ACTUATOR_HEALTH_START_DELAY_MS = 40_000;
    public static final int ACTUATOR_HEALTH_INTERVAL_SECONDS = 30;
    public static final int ACTUATOR_HEALTH_TIMEOUT_SECONDS = 3;
    public static final int ACTUATOR_HEALTH_RETRIES = 3;
}
