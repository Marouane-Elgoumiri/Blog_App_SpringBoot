package com.example.blog_app_springboot.feed.controller;

import com.example.blog_app_springboot.articles.dtos.ArticleResponse;
import com.example.blog_app_springboot.common.dtos.ApiResponse;
import com.example.blog_app_springboot.common.dtos.PageResponse;
import com.example.blog_app_springboot.feed.service.FeedService;
import com.example.blog_app_springboot.security.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;
    private final SecurityUtil securityUtil;

    @GetMapping("/feed")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<ArticleResponse>>> getFeed(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        var response = feedService.getFeed(securityUtil.getCurrentUserId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/articles/popular")
    public ResponseEntity<ApiResponse<PageResponse<ArticleResponse>>> getPopularArticles(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        var response = feedService.getPopularArticles(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
