package com.example.blog_app_springboot.analytics.service;

import com.example.blog_app_springboot.analytics.entity.ArticleViewEntity;
import com.example.blog_app_springboot.analytics.repository.ArticleViewRepository;
import com.example.blog_app_springboot.articles.dtos.ArticleResponse;
import com.example.blog_app_springboot.articles.entity.ArticleEntity;
import com.example.blog_app_springboot.articles.entity.ArticleStatus;
import com.example.blog_app_springboot.articles.repository.ArticleRepository;
import com.example.blog_app_springboot.common.constants.AppConstants;
import com.example.blog_app_springboot.common.dtos.PageResponse;
import com.example.blog_app_springboot.common.exceptions.ResourceNotFoundException;
import com.example.blog_app_springboot.common.utils.ReadingTimeCalculator;
import com.example.blog_app_springboot.comments.repository.CommentRepository;
import com.example.blog_app_springboot.interactions.service.InteractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ViewCountService {

    private static final int VIEW_DEDUP_HOURS = 24;

    private final ArticleViewRepository articleViewRepository;
    private final ArticleRepository articleRepository;
    private final InteractionService interactionService;
    private final ReadingTimeCalculator readingTimeCalculator;
    private final CommentRepository commentRepository;

    @Transactional
    public void recordView(Long articleId, String viewerIp) {
        LocalDateTime since = LocalDateTime.now().minusHours(VIEW_DEDUP_HOURS);

        if (articleViewRepository.hasRecentView(articleId, viewerIp, since)) {
            return;
        }

        var article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "id", articleId));

        var view = ArticleViewEntity.builder()
                .article(article)
                .viewerIp(viewerIp)
                .viewedAt(LocalDateTime.now())
                .build();

        articleViewRepository.save(view);
    }

    @Transactional(readOnly = true)
    public long getViewCount(Long articleId) {
        return articleViewRepository.countByArticleId(articleId);
    }

    @Transactional(readOnly = true)
    public PageResponse<ArticleResponse> getPopularArticles(Pageable pageable) {
        List<Object[]> viewCounts = articleViewRepository.countViewsGroupedByArticle();

        if (viewCounts.isEmpty()) {
            Page<ArticleEntity> articles = articleRepository.findByStatus(ArticleStatus.PUBLISHED, pageable);
            return PageResponse.from(articles.map(this::toResponse));
        }

        List<Long> articleIds = viewCounts.stream()
                .map(row -> (Long) row[0])
                .collect(Collectors.toList());

        List<ArticleEntity> articles = articleRepository.findAllById(articleIds).stream()
                .filter(a -> a.getStatus() == ArticleStatus.PUBLISHED)
                .sorted(Comparator.comparingLong((ArticleEntity a) ->
                        viewCounts.stream()
                                .filter(row -> row[0].equals(a.getId()))
                                .mapToLong(row -> (Long) row[1])
                                .findFirst()
                                .orElse(0)).reversed())
                .skip(pageable.getOffset())
                .limit(pageable.getPageSize())
                .collect(Collectors.toList());

        return PageResponse.from(articles.stream()
                .map(this::toResponse)
                .collect(Collectors.toList()), pageable);
    }

    private ArticleResponse toResponse(ArticleEntity article) {
        Long currentUserId = getCurrentUserId();
        boolean liked = currentUserId != null && interactionService.hasLiked(article.getId(), currentUserId);
        boolean bookmarked = currentUserId != null && interactionService.hasBookmarked(article.getId(), currentUserId);
        long likeCount = interactionService.getLikeCount(article.getId());
        long viewCount = articleViewRepository.countByArticleId(article.getId());
        int commentCount = commentRepository.findByArticleIdAndParentIsNullOrderByCreatedAtDesc(article.getId()).size();

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
                .readingTimeMinutes(readingTimeCalculator.calculate(article.getBody()))
                .likeCount(likeCount)
                .likedByCurrentUser(liked)
                .bookmarkedByCurrentUser(bookmarked)
                .commentCount(commentCount)
                .viewCount(viewCount)
                .build();
    }

    private Long getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long userId) {
            return userId;
        }
        return null;
    }
}
