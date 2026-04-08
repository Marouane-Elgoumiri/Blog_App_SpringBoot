package com.example.blog_app_springboot.articles.service;

import com.example.blog_app_springboot.articles.dtos.ArticleResponse;
import com.example.blog_app_springboot.articles.dtos.CreateArticleRequest;
import com.example.blog_app_springboot.articles.dtos.UpdateArticleRequest;
import com.example.blog_app_springboot.common.dtos.PageResponse;
import org.springframework.data.domain.Pageable;

public interface ArticleService {
	PageResponse<ArticleResponse> getPublishedArticles(Pageable pageable);
	ArticleResponse getArticleBySlug(String slug, Long currentUserId);
	PageResponse<ArticleResponse> getMyArticles(Long userId, Pageable pageable);
	ArticleResponse createArticle(CreateArticleRequest request, Long authorId);
	ArticleResponse updateArticle(Long articleId, UpdateArticleRequest request, Long userId);
	void deleteArticle(Long articleId, Long userId);
	ArticleResponse updateArticleBySlug(String slug, UpdateArticleRequest request, Long userId);
	void deleteArticleBySlug(String slug, Long userId);
}
