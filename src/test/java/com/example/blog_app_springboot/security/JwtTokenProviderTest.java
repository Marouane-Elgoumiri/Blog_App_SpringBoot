package com.example.blog_app_springboot.security;

import com.example.blog_app_springboot.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private static final String SECRET = "test-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm";
    private static final long ACCESS_EXPIRATION = 900000;
    private static final long REFRESH_EXPIRATION = 604800000;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(SECRET, ACCESS_EXPIRATION, REFRESH_EXPIRATION);
    }

    @Test
    void generates_valid_access_token() {
        String token = jwtTokenProvider.generateAccessToken(1L, "testuser", Set.of("USER"));

        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.isTokenValid(token)).isTrue();
    }

    @Test
    void generates_valid_refresh_token() {
        String token = jwtTokenProvider.generateRefreshToken(1L, "testuser", Set.of("USER"));

        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.isTokenValid(token)).isTrue();
    }

    @Test
    void extracts_user_id_from_token() {
        String token = jwtTokenProvider.generateAccessToken(42L, "testuser", Set.of("USER"));

        Long userId = jwtTokenProvider.getUserIdFromToken(token);

        assertThat(userId).isEqualTo(42L);
    }

    @Test
    void extracts_username_from_token() {
        String token = jwtTokenProvider.generateAccessToken(1L, "myuser", Set.of("USER"));

        String username = jwtTokenProvider.getUsernameFromToken(token);

        assertThat(username).isEqualTo("myuser");
    }

    @Test
    void extracts_roles_from_token() {
        String token = jwtTokenProvider.generateAccessToken(1L, "admin", Set.of("USER", "ADMIN"));

        Set<String> roles = jwtTokenProvider.getRolesFromToken(token);

        assertThat(roles).containsExactlyInAnyOrder("USER", "ADMIN");
    }

    @Test
    void returns_false_for_invalid_token() {
        assertThat(jwtTokenProvider.isTokenValid("invalid.token.here")).isFalse();
    }

    @Test
    void returns_false_for_empty_token() {
        assertThat(jwtTokenProvider.isTokenValid("")).isFalse();
    }

    @Test
    void returns_false_for_malformed_token() {
        assertThat(jwtTokenProvider.isTokenValid("not-a-jwt")).isFalse();
    }

    @Test
    void tokens_with_different_secrets_are_invalid() {
        JwtTokenProvider otherProvider = new JwtTokenProvider(
                "different-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm",
                ACCESS_EXPIRATION, REFRESH_EXPIRATION);

        String token = jwtTokenProvider.generateAccessToken(1L, "testuser", Set.of("USER"));

        assertThat(otherProvider.isTokenValid(token)).isFalse();
    }
}
