package com.example.blog_app_springboot.feed;

import com.example.blog_app_springboot.articles.dtos.ArticleResponse;
import com.example.blog_app_springboot.articles.entity.ArticleEntity;
import com.example.blog_app_springboot.articles.entity.ArticleStatus;
import com.example.blog_app_springboot.articles.repository.ArticleRepository;
import com.example.blog_app_springboot.common.dtos.PageResponse;
import com.example.blog_app_springboot.feed.service.FeedService;
import com.example.blog_app_springboot.follows.entity.FollowEntity;
import com.example.blog_app_springboot.follows.repository.FollowRepository;
import com.example.blog_app_springboot.users.entity.UserEntity;
import com.example.blog_app_springboot.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FollowRepository followRepository;

    @InjectMocks
    private FeedService feedService;

    private UserEntity user;
    private UserEntity followedUser;
    private ArticleEntity article;

    @BeforeEach
    void setUp() {
        user = UserEntity.builder()
                .id(1L)
                .username("user")
                .password("hashed")
                .email("user@example.com")
                .roles(Set.of(UserEntity.Role.USER))
                .build();

        followedUser = UserEntity.builder()
                .id(2L)
                .username("followed")
                .password("hashed")
                .email("followed@example.com")
                .roles(Set.of(UserEntity.Role.USER))
                .build();

        article = ArticleEntity.builder()
                .id(1L)
                .title("Followed Article")
                .slug("followed-article")
                .body("Body content here")
                .author(followedUser)
                .status(ArticleStatus.PUBLISHED)
                .tags(new HashSet<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void can_get_feed_with_followed_users() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(followRepository.findByFollowerId(1L)).thenReturn(List.of(
                FollowEntity.builder().follower(user).following(followedUser).build()
        ));
        when(articleRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(article)));

        PageResponse<ArticleResponse> result = feedService.getFeed(1L, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Followed Article");
    }

    @Test
    void can_get_feed_when_no_follows() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(followRepository.findByFollowerId(1L)).thenReturn(List.of());
        when(articleRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(Page.empty());

        PageResponse<ArticleResponse> result = feedService.getFeed(1L, PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void can_get_popular_articles() {
        when(articleRepository.findByStatus(ArticleStatus.PUBLISHED, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(article)));

        PageResponse<ArticleResponse> result = feedService.getPopularArticles(PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Followed Article");
    }
}
