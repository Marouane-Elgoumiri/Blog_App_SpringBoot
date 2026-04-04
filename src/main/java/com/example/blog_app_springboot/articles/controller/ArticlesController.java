package com.example.blog_app_springboot.articles.controller;

import com.example.blog_app_springboot.articles.dtos.ArticleResponse;
import com.example.blog_app_springboot.articles.dtos.CreateArticleRequest;
import com.example.blog_app_springboot.articles.dtos.UpdateArticleRequest;
import com.example.blog_app_springboot.articles.service.ArticleService;
import com.example.blog_app_springboot.common.dtos.ApiResponse;
import com.example.blog_app_springboot.common.dtos.PageResponse;
import com.example.blog_app_springboot.search.SearchService;
import com.example.blog_app_springboot.security.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/articles")
@RequiredArgsConstructor
public class ArticlesController {

    private final ArticleService articleService;
    private final SearchService searchService;
    private final SecurityUtil securityUtil;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ArticleResponse>>> getArticles(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        var response = articleService.getPublishedArticles(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<ArticleResponse>> getArticle(@PathVariable String slug) {
        var response = articleService.getArticleBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<ArticleResponse>>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String author,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        var response = searchService.search(q, tag, author, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ArticleResponse>> createArticle(
            @Valid @RequestBody CreateArticleRequest request) {
        var response = articleService.createArticle(request, securityUtil.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Article created successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ArticleResponse>> updateArticle(
            @PathVariable Long id,
            @Valid @RequestBody UpdateArticleRequest request) {
        var response = articleService.updateArticle(id, request, securityUtil.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success("Article updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id, securityUtil.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<ArticleResponse>>> getMyArticles(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        var response = articleService.getMyArticles(securityUtil.getCurrentUserId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
