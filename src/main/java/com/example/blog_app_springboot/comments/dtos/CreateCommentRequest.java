package com.example.blog_app_springboot.comments.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCommentRequest {
    private String title;

    @NotBlank(message = "Comment body is required")
    @Size(max = 2000, message = "Comment body must not exceed 2000 characters")
    private String body;

    private Long parentId;
}
