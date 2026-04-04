package com.example.blog_app_springboot.interactions.service;

import com.example.blog_app_springboot.articles.repository.ArticleRepository;
import com.example.blog_app_springboot.common.exceptions.BadRequestException;
import com.example.blog_app_springboot.common.exceptions.ResourceNotFoundException;
import com.example.blog_app_springboot.interactions.dtos.InteractionResponse;
import com.example.blog_app_springboot.interactions.entity.BookmarkEntity;
import com.example.blog_app_springboot.interactions.entity.LikeEntity;
import com.example.blog_app_springboot.interactions.repository.BookmarkRepository;
import com.example.blog_app_springboot.interactions.repository.LikeRepository;
import com.example.blog_app_springboot.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InteractionService {

    private final LikeRepository likeRepository;
    private final BookmarkRepository bookmarkRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    @Transactional
    public InteractionResponse toggleLike(Long articleId, Long userId) {
        var article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "id", articleId));
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        var existingLike = likeRepository.findByUserIdAndArticleId(userId, articleId);

        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
            long count = likeRepository.countByArticleId(articleId);
            return InteractionResponse.builder().action(false).count(count).build();
        } else {
            var like = LikeEntity.builder()
                    .user(user)
                    .article(article)
                    .build();
            likeRepository.save(like);
            long count = likeRepository.countByArticleId(articleId);
            return InteractionResponse.builder().action(true).count(count).build();
        }
    }

    @Transactional
    public InteractionResponse toggleBookmark(Long articleId, Long userId) {
        var article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "id", articleId));
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        var existingBookmark = bookmarkRepository.findByUserIdAndArticleId(userId, articleId);

        if (existingBookmark.isPresent()) {
            bookmarkRepository.delete(existingBookmark.get());
            return InteractionResponse.builder().action(false).count(0).build();
        } else {
            var bookmark = BookmarkEntity.builder()
                    .user(user)
                    .article(article)
                    .build();
            bookmarkRepository.save(bookmark);
            return InteractionResponse.builder().action(true).count(0).build();
        }
    }

    @Transactional(readOnly = true)
    public boolean hasLiked(Long articleId, Long userId) {
        return likeRepository.existsByUserIdAndArticleId(userId, articleId);
    }

    @Transactional(readOnly = true)
    public boolean hasBookmarked(Long articleId, Long userId) {
        return bookmarkRepository.existsByUserIdAndArticleId(userId, articleId);
    }

    @Transactional(readOnly = true)
    public long getLikeCount(Long articleId) {
        return likeRepository.countByArticleId(articleId);
    }
}
