package com.example.blog_app_springboot.users.dtos;

import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String username;

    private String password;

    private String email;

    private String bio;

    private String image;

}
