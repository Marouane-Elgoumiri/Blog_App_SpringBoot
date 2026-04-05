package com.example.blog_app_springboot.common.constants;

public final class SecurityConstants {

    private SecurityConstants() {
    }

    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    public static final String BEARER_PREFIX = "Bearer ";
    public static final String AUTHORIZATION_HEADER = "Authorization";

    public static final String HSTS_MAX_AGE_SECONDS = "31536000";
    public static final String CSP_POLICY = "default-src 'self'";
    public static final String PERMISSIONS_POLICY = "camera=(), microphone=(), geolocation=()";

    public static final String[] PUBLIC_AUTH_ENDPOINTS = {
            "/api/v1/auth/**"
    };

    public static final String[] PUBLIC_GET_ENDPOINTS = {
            "/api/v1/users/{id}",
            "/api/v1/articles/**",
            "/api/v1/articles/{slug}/comments",
            "/api/v1/articles/{slug}/comments/{commentId}/replies",
            "/api/v1/tags"
    };

    public static final String[] SWAGGER_ENDPOINTS = {
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-ui.html"
    };

    public static final String[] ACTUATOR_PUBLIC_ENDPOINTS = {
            "/actuator/health",
            "/actuator/info"
    };

    public static final String[] EXCLUDED_FROM_FILTERS = {
            "/actuator/",
            "/h2-console/",
            "/swagger-ui/",
            "/v3/api-docs/"
    };
}
