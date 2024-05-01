package com.example.blog_app_springboot.articles.dtos;

import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.Setter;

@Data
@Setter(AccessLevel.NONE)
public class CreateArticleRequest {
    @NonNull
    private String title;
    @NonNull
    private String body;
    @Nullable
    private String subtitle;

}
