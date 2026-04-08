package com.example.blog_app_springboot.security.config;

import com.example.blog_app_springboot.common.constants.SecurityConstants;
import com.example.blog_app_springboot.config.RateLimitingFilter;
import com.example.blog_app_springboot.security.jwt.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final RateLimitingFilter rateLimitingFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          CustomAuthenticationEntryPoint authenticationEntryPoint,
                          @Autowired(required = false) RateLimitingFilter rateLimitingFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.rateLimitingFilter = rateLimitingFilter;
    }

@Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(org.springframework.security.config.Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint))
                .authorizeHttpRequests(auth -> {
                    for (String pattern : SecurityConstants.PUBLIC_AUTH_ENDPOINTS) {
                        auth.requestMatchers(pattern).permitAll();
                    }
                    auth.requestMatchers(HttpMethod.POST, "/api/v1/users").permitAll();
                    auth.requestMatchers(HttpMethod.GET, "/api/v1/users/{id}").permitAll();
                    auth.requestMatchers("/h2-console/**").permitAll();
                    for (String pattern : SecurityConstants.SWAGGER_ENDPOINTS) {
                        auth.requestMatchers(pattern).permitAll();
                    }
                    for (String pattern : SecurityConstants.ACTUATOR_PUBLIC_ENDPOINTS) {
                        auth.requestMatchers(pattern).permitAll();
                    }
                    auth.requestMatchers(HttpMethod.GET, "/api/v1/articles/**").permitAll();
                    auth.requestMatchers(HttpMethod.GET, "/api/v1/articles/{slug}/comments").permitAll();
                    auth.requestMatchers(HttpMethod.GET, "/api/v1/articles/{slug}/comments/{commentId}/replies").permitAll();
                    auth.requestMatchers(HttpMethod.GET, "/api/v1/tags").permitAll();
                    auth.anyRequest().authenticated();
                })
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                        .contentTypeOptions(org.springframework.security.config.Customizer.withDefaults())
                        .httpStrictTransportSecurity(hsts -> hsts
                                .maxAgeInSeconds(Long.parseLong(SecurityConstants.HSTS_MAX_AGE_SECONDS))
                                .includeSubDomains(true))
                        .contentSecurityPolicy(csp -> csp.policyDirectives(SecurityConstants.CSP_POLICY))
                        .referrerPolicy(referrer -> referrer.policy(ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        .permissionsPolicy(permissions -> permissions.policy(SecurityConstants.PERMISSIONS_POLICY))
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        if (rateLimitingFilter != null) {
            http.addFilterBefore(rateLimitingFilter, JwtAuthFilter.class);
        }

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
