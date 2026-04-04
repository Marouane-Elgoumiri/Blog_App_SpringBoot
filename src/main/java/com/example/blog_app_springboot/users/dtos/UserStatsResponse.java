package com.example.blog_app_springboot.users.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserStatsResponse {
    private long totalArticles;
    private long publishedArticles;
    private long draftArticles;
    private long totalComments;
    private long totalLikes;
    private long totalFollowers;
    private long totalFollowing;
}
