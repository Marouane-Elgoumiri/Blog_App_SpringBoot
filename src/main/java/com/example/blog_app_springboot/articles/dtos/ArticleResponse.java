package com.example.blog_app_springboot.articles.dtos;

import com.example.blog_app_springboot.articles.entity.ArticleStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ArticleResponse {
    private Long id;
    private String title;
    private String slug;
    private String subtitle;
    private String body;
    private ArticleStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private AuthorInfo author;
    private List<TagInfo> tags;
    private int readingTimeMinutes;
    private long likeCount;
    private boolean likedByCurrentUser;
    private boolean bookmarkedByCurrentUser;
    private int commentCount;
    private long viewCount;

    @Data
    @Builder
    public static class AuthorInfo {
        private Long id;
        private String username;
        private String image;
    }

    @Data
    @Builder
    public static class TagInfo {
        private Long id;
        private String name;
        private String slug;
    }
}
