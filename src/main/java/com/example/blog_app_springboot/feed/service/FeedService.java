package com.example.blog_app_springboot.feed.service;

import com.example.blog_app_springboot.articles.dtos.ArticleResponse;
import com.example.blog_app_springboot.articles.entity.ArticleEntity;
import com.example.blog_app_springboot.articles.entity.ArticleStatus;
import com.example.blog_app_springboot.articles.repository.ArticleRepository;
import com.example.blog_app_springboot.common.constants.AppConstants;
import com.example.blog_app_springboot.common.dtos.PageResponse;
import com.example.blog_app_springboot.common.exceptions.ResourceNotFoundException;
import com.example.blog_app_springboot.follows.repository.FollowRepository;
import com.example.blog_app_springboot.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    @Transactional(readOnly = true)
    public PageResponse<ArticleResponse> getFeed(Long userId, Pageable pageable) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        List<Long> followingIds = followRepository.findByFollowerId(userId).stream()
                .map(f -> f.getFollowing().getId())
                .toList();

        Specification<ArticleEntity> spec = Specification.where(byStatus(ArticleStatus.PUBLISHED));

        if (!followingIds.isEmpty()) {
            spec = spec.and(byAuthorIds(followingIds));
        }

        Page<ArticleEntity> articles = articleRepository.findAll(spec, pageable);
        return PageResponse.from(articles.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PageResponse<ArticleResponse> getPopularArticles(Pageable pageable) {
        var articles = articleRepository.findByStatus(ArticleStatus.PUBLISHED, pageable);
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

    private Specification<ArticleEntity> byStatus(ArticleStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    private Specification<ArticleEntity> byAuthorIds(List<Long> authorIds) {
        return (root, query, cb) -> root.get("author").get("id").in(authorIds);
    }
}
