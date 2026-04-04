package com.example.blog_app_springboot.articles;

import com.example.blog_app_springboot.articles.dtos.ArticleResponse;
import com.example.blog_app_springboot.articles.dtos.CreateArticleRequest;
import com.example.blog_app_springboot.articles.dtos.UpdateArticleRequest;
import com.example.blog_app_springboot.articles.entity.ArticleEntity;
import com.example.blog_app_springboot.articles.entity.ArticleStatus;
import com.example.blog_app_springboot.articles.repository.ArticleRepository;
import com.example.blog_app_springboot.articles.service.ArticleServiceImpl;
import com.example.blog_app_springboot.comments.repository.CommentRepository;
import com.example.blog_app_springboot.common.exceptions.ResourceNotFoundException;
import com.example.blog_app_springboot.common.exceptions.UnauthorizedException;
import com.example.blog_app_springboot.common.utils.ReadingTimeCalculator;
import com.example.blog_app_springboot.common.utils.SlugGenerator;
import com.example.blog_app_springboot.interactions.service.InteractionService;
import com.example.blog_app_springboot.tags.entity.TagEntity;
import com.example.blog_app_springboot.tags.service.TagService;
import com.example.blog_app_springboot.users.entity.UserEntity;
import com.example.blog_app_springboot.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SlugGenerator slugGenerator;

    @Mock
    private TagService tagService;

    @Mock
    private ReadingTimeCalculator readingTimeCalculator;

    @Mock
    private InteractionService interactionService;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private ArticleServiceImpl articleService;

    private UserEntity author;
    private ArticleEntity sampleArticle;

    @BeforeEach
    void setUp() {
        var auth = mock(Authentication.class);
        lenient().when(auth.getPrincipal()).thenReturn(1L);
        var context = mock(SecurityContext.class);
        lenient().when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        author = UserEntity.builder()
                .id(1L)
                .username("author")
                .password("hashed")
                .email("author@example.com")
                .roles(Set.of(UserEntity.Role.USER))
                .build();

        sampleArticle = ArticleEntity.builder()
                .id(1L)
                .title("Test Article")
                .slug("test-article")
                .subtitle("Test Subtitle")
                .body("Test body content")
                .author(author)
                .status(ArticleStatus.DRAFT)
                .build();
    }

    @Test
    void can_create_article() {
        CreateArticleRequest request = new CreateArticleRequest();
        request.setTitle("New Article");
        request.setBody("New body");
        request.setSubtitle("New subtitle");

        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(slugGenerator.generateUniqueSlug(eq("New Article"), any())).thenReturn("new-article");
        when(tagService.findOrCreateTags(any())).thenReturn(List.of());
        when(articleRepository.save(any(ArticleEntity.class))).thenAnswer(invocation -> {
            ArticleEntity saved = invocation.getArgument(0);
            saved.setId(2L);
            saved.setCreatedAt(LocalDateTime.now());
            saved.setUpdatedAt(LocalDateTime.now());
            return saved;
        });
        when(readingTimeCalculator.calculate(any())).thenReturn(1);
        when(interactionService.hasLiked(anyLong(), anyLong())).thenReturn(false);
        when(interactionService.hasBookmarked(anyLong(), anyLong())).thenReturn(false);
        when(interactionService.getLikeCount(anyLong())).thenReturn(0L);

        ArticleResponse response = articleService.createArticle(request, 1L);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("New Article");
        assertThat(response.getSlug()).isEqualTo("new-article");
        assertThat(response.getStatus()).isEqualTo(ArticleStatus.DRAFT);
        verify(articleRepository).save(any(ArticleEntity.class));
    }

    @Test
    void throws_when_author_not_found() {
        CreateArticleRequest request = new CreateArticleRequest();
        request.setTitle("Test");
        request.setBody("Body");

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> articleService.createArticle(request, 999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void can_get_article_by_slug() {
        when(articleRepository.findBySlug("test-article")).thenReturn(Optional.of(sampleArticle));
        when(readingTimeCalculator.calculate(any())).thenReturn(1);
        when(interactionService.hasLiked(anyLong(), anyLong())).thenReturn(false);
        when(interactionService.hasBookmarked(anyLong(), anyLong())).thenReturn(false);
        when(interactionService.getLikeCount(anyLong())).thenReturn(0L);

        ArticleResponse response = articleService.getArticleBySlug("test-article");

        assertThat(response).isNotNull();
        assertThat(response.getSlug()).isEqualTo("test-article");
        assertThat(response.getTitle()).isEqualTo("Test Article");
    }

    @Test
    void throws_when_article_not_found_by_slug() {
        when(articleRepository.findBySlug("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> articleService.getArticleBySlug("missing"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Article not found");
    }

    @Test
    void can_update_own_article() {
        UpdateArticleRequest request = new UpdateArticleRequest();
        request.setTitle("Updated Title");
        request.setBody("Updated body");

        when(articleRepository.findById(1L)).thenReturn(Optional.of(sampleArticle));
        when(slugGenerator.generateUniqueSlug(eq("Updated Title"), any())).thenReturn("updated-title");
        when(articleRepository.save(any(ArticleEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(readingTimeCalculator.calculate(any())).thenReturn(1);
        when(interactionService.hasLiked(anyLong(), anyLong())).thenReturn(false);
        when(interactionService.hasBookmarked(anyLong(), anyLong())).thenReturn(false);
        when(interactionService.getLikeCount(anyLong())).thenReturn(0L);

        ArticleResponse response = articleService.updateArticle(1L, request, 1L);

        assertThat(response.getTitle()).isEqualTo("Updated Title");
        assertThat(response.getSlug()).isEqualTo("updated-title");
        assertThat(response.getBody()).isEqualTo("Updated body");
    }

    @Test
    void throws_when_updating_others_article() {
        UpdateArticleRequest request = new UpdateArticleRequest();
        request.setTitle("Hacked");

        when(articleRepository.findById(1L)).thenReturn(Optional.of(sampleArticle));

        assertThatThrownBy(() -> articleService.updateArticle(1L, request, 999L))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("You can only edit your own articles");
    }

    @Test
    void can_delete_own_article() {
        when(articleRepository.findById(1L)).thenReturn(Optional.of(sampleArticle));

        articleService.deleteArticle(1L, 1L);

        verify(articleRepository).delete(sampleArticle);
    }

    @Test
    void throws_when_deleting_others_article() {
        when(articleRepository.findById(1L)).thenReturn(Optional.of(sampleArticle));

        assertThatThrownBy(() -> articleService.deleteArticle(1L, 999L))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("You can only delete your own articles");
    }
}
