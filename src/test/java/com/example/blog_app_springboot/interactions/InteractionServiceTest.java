package com.example.blog_app_springboot.interactions;

import com.example.blog_app_springboot.articles.entity.ArticleEntity;
import com.example.blog_app_springboot.articles.entity.ArticleStatus;
import com.example.blog_app_springboot.articles.repository.ArticleRepository;
import com.example.blog_app_springboot.common.exceptions.ResourceNotFoundException;
import com.example.blog_app_springboot.interactions.dtos.InteractionResponse;
import com.example.blog_app_springboot.interactions.entity.LikeEntity;
import com.example.blog_app_springboot.interactions.repository.BookmarkRepository;
import com.example.blog_app_springboot.interactions.repository.LikeRepository;
import com.example.blog_app_springboot.interactions.service.InteractionService;
import com.example.blog_app_springboot.users.entity.UserEntity;
import com.example.blog_app_springboot.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InteractionServiceTest {

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private BookmarkRepository bookmarkRepository;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private InteractionService interactionService;

    private UserEntity user;
    private ArticleEntity article;

    @BeforeEach
    void setUp() {
        user = UserEntity.builder()
                .id(1L)
                .username("testuser")
                .password("hashed")
                .email("test@example.com")
                .roles(Set.of(UserEntity.Role.USER))
                .build();

        article = ArticleEntity.builder()
                .id(1L)
                .title("Test Article")
                .slug("test-article")
                .body("Test body")
                .author(user)
                .status(ArticleStatus.PUBLISHED)
                .build();
    }

    @Test
    void can_toggle_like_creates_like() {
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(likeRepository.findByUserIdAndArticleId(1L, 1L)).thenReturn(Optional.empty());
        when(likeRepository.countByArticleId(1L)).thenReturn(1L);

        InteractionResponse result = interactionService.toggleLike(1L, 1L);

        assertThat(result.isAction()).isTrue();
        assertThat(result.getCount()).isEqualTo(1L);
        verify(likeRepository).save(any(LikeEntity.class));
    }

    @Test
    void can_toggle_like_removes_existing_like() {
        var existingLike = LikeEntity.builder()
                .id(1L)
                .user(user)
                .article(article)
                .build();

        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(likeRepository.findByUserIdAndArticleId(1L, 1L)).thenReturn(Optional.of(existingLike));
        when(likeRepository.countByArticleId(1L)).thenReturn(0L);

        InteractionResponse result = interactionService.toggleLike(1L, 1L);

        assertThat(result.isAction()).isFalse();
        assertThat(result.getCount()).isEqualTo(0L);
        verify(likeRepository).delete(existingLike);
    }

    @Test
    void can_toggle_bookmark_creates_bookmark() {
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookmarkRepository.findByUserIdAndArticleId(1L, 1L)).thenReturn(Optional.empty());

        InteractionResponse result = interactionService.toggleBookmark(1L, 1L);

        assertThat(result.isAction()).isTrue();
        verify(bookmarkRepository).save(any());
    }

    @Test
    void can_toggle_bookmark_removes_existing_bookmark() {
        var existingBookmark = com.example.blog_app_springboot.interactions.entity.BookmarkEntity.builder()
                .id(1L)
                .user(user)
                .article(article)
                .build();

        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookmarkRepository.findByUserIdAndArticleId(1L, 1L)).thenReturn(Optional.of(existingBookmark));

        InteractionResponse result = interactionService.toggleBookmark(1L, 1L);

        assertThat(result.isAction()).isFalse();
        verify(bookmarkRepository).delete(existingBookmark);
    }

    @Test
    void throws_when_article_not_found_for_like() {
        when(articleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interactionService.toggleLike(999L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Article not found");
    }

    @Test
    void returns_true_when_user_has_liked() {
        when(likeRepository.existsByUserIdAndArticleId(1L, 1L)).thenReturn(true);

        assertThat(interactionService.hasLiked(1L, 1L)).isTrue();
    }

    @Test
    void returns_false_when_user_has_not_liked() {
        when(likeRepository.existsByUserIdAndArticleId(1L, 1L)).thenReturn(false);

        assertThat(interactionService.hasLiked(1L, 1L)).isFalse();
    }

    @Test
    void returns_true_when_user_has_bookmarked() {
        when(bookmarkRepository.existsByUserIdAndArticleId(1L, 1L)).thenReturn(true);

        assertThat(interactionService.hasBookmarked(1L, 1L)).isTrue();
    }

    @Test
    void returns_like_count() {
        when(likeRepository.countByArticleId(1L)).thenReturn(5L);

        assertThat(interactionService.getLikeCount(1L)).isEqualTo(5L);
    }
}
