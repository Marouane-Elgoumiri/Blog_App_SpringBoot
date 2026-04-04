package com.example.blog_app_springboot.users.dtos;

import com.example.blog_app_springboot.users.entity.UserEntity;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String bio;
    private String image;
    private Set<UserEntity.Role> roles;
    private long followerCount;
    private long followingCount;

    public static UserResponse from(UserEntity user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .bio(user.getBio())
                .image(user.getImage())
                .roles(user.getRoles())
                .followerCount(0)
                .followingCount(0)
                .build();
    }

    public static UserResponse from(UserEntity user, long followerCount, long followingCount) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .bio(user.getBio())
                .image(user.getImage())
                .roles(user.getRoles())
                .followerCount(followerCount)
                .followingCount(followingCount)
                .build();
    }
}
