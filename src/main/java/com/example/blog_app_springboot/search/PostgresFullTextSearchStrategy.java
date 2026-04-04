package com.example.blog_app_springboot.search;

import com.example.blog_app_springboot.articles.entity.ArticleEntity;
import com.example.blog_app_springboot.articles.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
@RequiredArgsConstructor
public class PostgresFullTextSearchStrategy implements SearchStrategy {

    private final ArticleRepository articleRepository;

    @Override
    public Page<ArticleEntity> search(String query, Pageable pageable) {
        return articleRepository.fullTextSearch(query, pageable);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
