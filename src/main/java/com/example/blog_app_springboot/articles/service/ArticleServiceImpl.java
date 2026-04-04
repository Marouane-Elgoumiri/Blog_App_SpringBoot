package com.example.blog_app_springboot.articles.service;

import com.example.blog_app_springboot.articles.dtos.ArticleResponse;
import com.example.blog_app_springboot.articles.dtos.CreateArticleRequest;
import com.example.blog_app_springboot.articles.dtos.UpdateArticleRequest;
import com.example.blog_app_springboot.articles.entity.ArticleEntity;
import com.example.blog_app_springboot.articles.entity.ArticleStatus;
import com.example.blog_app_springboot.articles.repository.ArticleRepository;
import com.example.blog_app_springboot.comments.repository.CommentRepository;
import com.example.blog_app_springboot.common.dtos.PageResponse;
import com.example.blog_app_springboot.common.exceptions.ResourceNotFoundException;
import com.example.blog_app_springboot.common.exceptions.UnauthorizedException;
import com.example.blog_app_springboot.common.utils.ReadingTimeCalculator;
import com.example.blog_app_springboot.common.utils.SlugGenerator;
import com.example.blog_app_springboot.interactions.service.InteractionService;
import com.example.blog_app_springboot.tags.entity.TagEntity;
import com.example.blog_app_springboot.tags.service.TagService;
import com.example.blog_app_springboot.users.entity.UserEntity;
import com.example.blog_app_springboot.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final SlugGenerator slugGenerator;
    private final TagService tagService;
    private final ReadingTimeCalculator readingTimeCalculator;
    private final InteractionService interactionService;
    private final CommentRepository commentRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ArticleResponse> getPublishedArticles(Pageable pageable) {
        Page<ArticleEntity> articles = articleRepository.findByStatus(ArticleStatus.PUBLISHED, pageable);
        return PageResponse.from(articles.map(this::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public ArticleResponse getArticleBySlug(String slug) {
        var article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "slug", slug));
        return toResponse(article);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ArticleResponse> getMyArticles(Long userId, Pageable pageable) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Page<ArticleEntity> articles = articleRepository.findAll(
                Specification.where(byAuthor(user)),
                pageable
        );
        return PageResponse.from(articles.map(this::toResponse));
    }

    @Override
    @Transactional
    public ArticleResponse createArticle(CreateArticleRequest request, Long authorId) {
        var author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", authorId));

        String slug = slugGenerator.generateUniqueSlug(request.getTitle(), articleRepository::existsBySlug);

        Set<TagEntity> tags = new HashSet<>(tagService.findOrCreateTags(request.getTags()));

        var article = ArticleEntity.builder()
                .title(request.getTitle())
                .slug(slug)
                .subtitle(request.getSubtitle())
                .body(request.getBody())
                .author(author)
                .status(ArticleStatus.DRAFT)
                .tags(tags)
                .build();

        var savedArticle = articleRepository.save(article);
        return toResponse(savedArticle);
    }

    @Override
    @Transactional
    public ArticleResponse updateArticle(Long articleId, UpdateArticleRequest request, Long userId) {
        var article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "id", articleId));

        if (!article.getAuthor().getId().equals(userId)) {
            throw new UnauthorizedException("You can only edit your own articles");
        }

        if (request.getTitle() != null) {
            article.setTitle(request.getTitle());
            String newSlug = slugGenerator.generateUniqueSlug(request.getTitle(), articleRepository::existsBySlug);
            article.setSlug(newSlug);
        }
        if (request.getSubtitle() != null) {
            article.setSubtitle(request.getSubtitle());
        }
        if (request.getBody() != null) {
            article.setBody(request.getBody());
        }
        if (request.getTags() != null) {
            article.getTags().clear();
            article.getTags().addAll(tagService.findOrCreateTags(request.getTags()));
        }

        var updatedArticle = articleRepository.save(article);
        return toResponse(updatedArticle);
    }

    @Override
    @Transactional
    public void deleteArticle(Long articleId, Long userId) {
        var article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "id", articleId));

        if (!article.getAuthor().getId().equals(userId)) {
            throw new UnauthorizedException("You can only delete your own articles");
        }

        articleRepository.delete(article);
    }

    private ArticleResponse toResponse(ArticleEntity article) {
        Long currentUserId = getCurrentUserId();
        boolean liked = currentUserId != null && interactionService.hasLiked(article.getId(), currentUserId);
        boolean bookmarked = currentUserId != null && interactionService.hasBookmarked(article.getId(), currentUserId);
        long likeCount = interactionService.getLikeCount(article.getId());
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
                .build();
    }

    private Long getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long userId) {
            return userId;
        }
        return null;
    }

    private Specification<ArticleEntity> byAuthor(UserEntity author) {
        return (root, query, cb) -> cb.equal(root.get("author"), author);
    }
}
