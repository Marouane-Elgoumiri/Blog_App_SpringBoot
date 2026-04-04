package com.example.blog_app_springboot.comments;

import com.example.blog_app_springboot.articles.entity.ArticleEntity;
import com.example.blog_app_springboot.articles.entity.ArticleStatus;
import com.example.blog_app_springboot.articles.repository.ArticleRepository;
import com.example.blog_app_springboot.comments.dtos.CommentResponse;
import com.example.blog_app_springboot.comments.dtos.CreateCommentRequest;
import com.example.blog_app_springboot.comments.entity.CommentEntity;
import com.example.blog_app_springboot.comments.repository.CommentRepository;
import com.example.blog_app_springboot.comments.service.CommentServiceImpl;
import com.example.blog_app_springboot.common.exceptions.ResourceNotFoundException;
import com.example.blog_app_springboot.common.exceptions.UnauthorizedException;
import com.example.blog_app_springboot.users.entity.UserEntity;
import com.example.blog_app_springboot.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CommentServiceImpl commentService;

    private UserEntity author;
    private UserEntity otherUser;
    private ArticleEntity article;
    private CommentEntity sampleComment;

    @BeforeEach
    void setUp() {
        author = UserEntity.builder()
                .id(1L)
                .username("author")
                .password("hashed")
                .email("author@example.com")
                .roles(Set.of(UserEntity.Role.USER))
                .build();

        otherUser = UserEntity.builder()
                .id(2L)
                .username("other")
                .password("hashed")
                .email("other@example.com")
                .roles(Set.of(UserEntity.Role.USER))
                .build();

        article = ArticleEntity.builder()
                .id(1L)
                .title("Test Article")
                .slug("test-article")
                .body("Test body")
                .author(author)
                .status(ArticleStatus.PUBLISHED)
                .build();

        sampleComment = CommentEntity.builder()
                .id(1L)
                .title("Test Comment")
                .body("Test body")
                .article(article)
                .author(author)
                .build();
    }

    @Test
    void can_create_comment() {
        CreateCommentRequest request = new CreateCommentRequest();
        request.setTitle("New Comment");
        request.setBody("Comment body");

        when(articleRepository.findBySlug("test-article")).thenReturn(Optional.of(article));
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(commentRepository.save(any(CommentEntity.class))).thenAnswer(invocation -> {
            CommentEntity saved = invocation.getArgument(0);
            saved.setId(2L);
            saved.setCreatedAt(LocalDateTime.now());
            saved.setUpdatedAt(LocalDateTime.now());
            return saved;
        });
        when(commentRepository.findByParentIdOrderByCreatedAtAsc(anyLong())).thenReturn(List.of());

        CommentResponse response = commentService.createComment("test-article", request, 1L);

        assertThat(response).isNotNull();
        assertThat(response.getBody()).isEqualTo("Comment body");
        assertThat(response.getAuthor().getUsername()).isEqualTo("author");
        verify(commentRepository).save(any(CommentEntity.class));
    }

    @Test
    void can_create_reply() {
        CommentEntity parentComment = CommentEntity.builder()
                .id(10L)
                .body("Parent comment")
                .article(article)
                .author(author)
                .build();

        CreateCommentRequest request = new CreateCommentRequest();
        request.setBody("Reply body");
        request.setParentId(10L);

        when(articleRepository.findBySlug("test-article")).thenReturn(Optional.of(article));
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(commentRepository.findById(10L)).thenReturn(Optional.of(parentComment));
        when(commentRepository.save(any(CommentEntity.class))).thenAnswer(invocation -> {
            CommentEntity saved = invocation.getArgument(0);
            saved.setId(11L);
            saved.setCreatedAt(LocalDateTime.now());
            saved.setUpdatedAt(LocalDateTime.now());
            return saved;
        });
        when(commentRepository.findByParentIdOrderByCreatedAtAsc(anyLong())).thenReturn(List.of());

        CommentResponse response = commentService.createComment("test-article", request, 1L);

        assertThat(response).isNotNull();
        assertThat(response.getBody()).isEqualTo("Reply body");
        verify(commentRepository).save(any(CommentEntity.class));
    }

    @Test
    void throws_when_article_not_found_for_comment() {
        CreateCommentRequest request = new CreateCommentRequest();
        request.setBody("Body");

        when(articleRepository.findBySlug("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.createComment("missing", request, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Article not found");
    }

    @Test
    void throws_when_parent_comment_not_found() {
        CreateCommentRequest request = new CreateCommentRequest();
        request.setBody("Reply");
        request.setParentId(999L);

        when(articleRepository.findBySlug("test-article")).thenReturn(Optional.of(article));
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.createComment("test-article", request, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Comment not found");
    }

    @Test
    void can_get_comments_by_article() {
        when(articleRepository.findBySlug("test-article")).thenReturn(Optional.of(article));
        when(commentRepository.findByArticleIdAndParentIsNullOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(sampleComment));
        when(commentRepository.findByParentIdOrderByCreatedAtAsc(1L)).thenReturn(List.of());

        List<CommentResponse> comments = commentService.getCommentsByArticle("test-article");

        assertThat(comments).hasSize(1);
        assertThat(comments.get(0).getBody()).isEqualTo("Test body");
    }

    @Test
    void returns_empty_when_no_comments() {
        when(articleRepository.findBySlug("test-article")).thenReturn(Optional.of(article));
        when(commentRepository.findByArticleIdAndParentIsNullOrderByCreatedAtDesc(1L))
                .thenReturn(List.of());

        List<CommentResponse> comments = commentService.getCommentsByArticle("test-article");

        assertThat(comments).isEmpty();
    }

    @Test
    void can_get_replies() {
        CommentEntity reply = CommentEntity.builder()
                .id(2L)
                .body("Reply body")
                .article(article)
                .author(otherUser)
                .parent(sampleComment)
                .build();

        when(commentRepository.findByParentIdOrderByCreatedAtAsc(1L))
                .thenReturn(List.of(reply));

        List<CommentResponse> replies = commentService.getReplies(1L);

        assertThat(replies).hasSize(1);
        assertThat(replies.get(0).getBody()).isEqualTo("Reply body");
    }

    @Test
    void can_delete_own_comment() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(sampleComment));

        commentService.deleteComment(1L, 1L);

        verify(commentRepository).delete(sampleComment);
    }

    @Test
    void throws_when_deleting_others_comment() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(sampleComment));

        assertThatThrownBy(() -> commentService.deleteComment(1L, 2L))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("You can only delete your own comments");
    }

    @Test
    void throws_when_comment_not_found_for_delete() {
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.deleteComment(999L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Comment not found");
    }
}
