package com.example.blog_app_springboot.security.auth;

import com.example.blog_app_springboot.users.dtos.UserResponse;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private UserDto user;

    @Data
    @Builder
    public static class UserDto {
        private Long id;
        private String username;
        private String email;
        private String bio;
        private String image;
        private java.util.Set<String> roles;

        public static UserDto from(com.example.blog_app_springboot.users.entity.UserEntity user) {
            return UserDto.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .bio(user.getBio())
                    .image(user.getImage())
                    .roles(user.getRoles().stream()
                            .map(Enum::name)
                            .collect(java.util.stream.Collectors.toSet()))
                    .build();
        }
    }
}
