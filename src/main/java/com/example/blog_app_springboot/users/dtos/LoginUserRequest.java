package com.example.blog_app_springboot.users.dtos;

import jakarta.annotation.Nullable;
import lombok.Data;
import lombok.NonNull;

@Data
public class LoginUserRequest {
    @NonNull
    private String username;
    @NonNull
    private String password;
    @Nullable
    private String email;

}
