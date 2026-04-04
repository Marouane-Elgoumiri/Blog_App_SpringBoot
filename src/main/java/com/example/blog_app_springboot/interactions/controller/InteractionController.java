package com.example.blog_app_springboot.interactions.controller;

import com.example.blog_app_springboot.articles.repository.ArticleRepository;
import com.example.blog_app_springboot.common.dtos.ApiResponse;
import com.example.blog_app_springboot.common.exceptions.ResourceNotFoundException;
import com.example.blog_app_springboot.interactions.dtos.InteractionResponse;
import com.example.blog_app_springboot.interactions.service.InteractionService;
import com.example.blog_app_springboot.security.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/articles/{slug}")
@RequiredArgsConstructor
public class InteractionController {

    private final InteractionService interactionService;
    private final SecurityUtil securityUtil;
    private final ArticleRepository articleRepository;

    @PostMapping("/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<InteractionResponse>> toggleLike(@PathVariable String slug) {
        var article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "slug", slug));
        var result = interactionService.toggleLike(article.getId(), securityUtil.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success("Like toggled", result));
    }

    @PostMapping("/bookmark")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<InteractionResponse>> toggleBookmark(@PathVariable String slug) {
        var article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "slug", slug));
        var result = interactionService.toggleBookmark(article.getId(), securityUtil.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success("Bookmark toggled", result));
    }
}
