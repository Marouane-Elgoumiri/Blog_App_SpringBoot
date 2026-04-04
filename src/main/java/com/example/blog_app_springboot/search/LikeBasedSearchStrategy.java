package com.example.blog_app_springboot.search;

import com.example.blog_app_springboot.articles.entity.ArticleEntity;
import com.example.blog_app_springboot.articles.entity.ArticleStatus;
import com.example.blog_app_springboot.articles.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
@Profile("!prod")
@RequiredArgsConstructor
public class LikeBasedSearchStrategy implements SearchStrategy {

    private final ArticleRepository articleRepository;

    @Override
    public Page<ArticleEntity> search(String query, Pageable pageable) {
        Specification<ArticleEntity> spec = Specification
                .where(ArticleSpecifications.hasStatus(ArticleStatus.PUBLISHED))
                .and(ArticleSpecifications.titleContains(query));
        return articleRepository.findAll(spec, pageable);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
