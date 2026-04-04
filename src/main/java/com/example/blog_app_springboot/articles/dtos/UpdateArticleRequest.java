package com.example.blog_app_springboot.articles.dtos;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdateArticleRequest {
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Size(max = 500, message = "Subtitle must not exceed 500 characters")
    private String subtitle;

    private String body;

    private List<String> tags;
}
