package com.example.blog_app_springboot.articles.dtos;

import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Setter(AccessLevel.NONE)
@Data
public class UpdateArticleRequest {
    @Nullable
    private String title;
    @Nullable
    private String body;
    @Nullable
    private String subtitle;
}
