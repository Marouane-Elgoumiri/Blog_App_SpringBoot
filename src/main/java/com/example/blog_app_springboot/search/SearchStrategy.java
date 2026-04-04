package com.example.blog_app_springboot.search;

import com.example.blog_app_springboot.articles.entity.ArticleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SearchStrategy {
    Page<ArticleEntity> search(String query, Pageable pageable);
    boolean isAvailable();
}
