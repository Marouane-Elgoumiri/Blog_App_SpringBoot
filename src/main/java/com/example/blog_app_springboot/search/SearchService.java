package com.example.blog_app_springboot.search;

import com.example.blog_app_springboot.articles.dtos.ArticleResponse;
import com.example.blog_app_springboot.articles.entity.ArticleEntity;
import com.example.blog_app_springboot.articles.entity.ArticleStatus;
import com.example.blog_app_springboot.articles.repository.ArticleRepository;
import com.example.blog_app_springboot.common.dtos.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final ArticleRepository articleRepository;
    private final List<SearchStrategy> searchStrategies;

    @Transactional(readOnly = true)
    public PageResponse<ArticleResponse> search(String query, String tag, String author, Pageable pageable) {
        boolean hasFullTextQuery = StringUtils.hasText(query);
        boolean hasTagFilter = StringUtils.hasText(tag);
        boolean hasAuthorFilter = StringUtils.hasText(author);

        Page<ArticleEntity> articles;

        if (hasFullTextQuery && !hasTagFilter && !hasAuthorFilter) {
            SearchStrategy strategy = searchStrategies.stream()
                    .filter(SearchStrategy::isAvailable)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No search strategy available"));
            articles = strategy.search(query.trim(), pageable);
        } else {
            Specification<ArticleEntity> spec = Specification.where(ArticleSpecifications.hasStatus(ArticleStatus.PUBLISHED));

            if (hasFullTextQuery) {
                spec = spec.and(ArticleSpecifications.titleContains(query));
            }
            if (hasTagFilter) {
                spec = spec.and(ArticleSpecifications.hasTag(tag));
            }
            if (hasAuthorFilter) {
                spec = spec.and(ArticleSpecifications.hasAuthor(author));
            }

            articles = articleRepository.findAll(spec, pageable);
        }

        return PageResponse.from(articles.map(this::toResponse));
    }

    private ArticleResponse toResponse(ArticleEntity article) {
        return ArticleResponse.builder()
                .id(article.getId())
                .title(article.getTitle())
                .slug(article.getSlug())
                .subtitle(article.getSubtitle())
                .body(article.getBody())
                .status(article.getStatus())
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .author(ArticleResponse.AuthorInfo.builder()
                        .id(article.getAuthor().getId())
                        .username(article.getAuthor().getUsername())
                        .image(article.getAuthor().getImage())
                        .build())
                .tags(article.getTags().stream()
                        .map(t -> ArticleResponse.TagInfo.builder()
                                .id(t.getId())
                                .name(t.getName())
                                .slug(t.getSlug())
                                .build())
                        .toList())
                .build();
    }
}
