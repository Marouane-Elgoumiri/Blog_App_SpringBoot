package com.example.blog_app_springboot.comments.dtos;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CommentResponse {
    private Long id;
    private String title;
    private String body;
    private LocalDateTime createdAt;
    private CommentAuthor author;
    private List<CommentResponse> replies;

    @Data
    @Builder
    public static class CommentAuthor {
        private Long id;
        private String username;
        private String image;
    }
}
