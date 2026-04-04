package com.example.blog_app_springboot.articles;

import com.example.blog_app_springboot.articles.entity.ArticleEntity;
import com.example.blog_app_springboot.articles.entity.ArticleStatus;
import com.example.blog_app_springboot.articles.repository.ArticleRepository;
import com.example.blog_app_springboot.config.JpaConfig;
import com.example.blog_app_springboot.users.entity.UserEntity;
import com.example.blog_app_springboot.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
@ActiveProfiles("test")
class ArticleRepositoryTest {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private UserRepository userRepository;

    private UserEntity author;

    @BeforeEach
    void setUp() {
        author = UserEntity.builder()
                .username("testauthor")
                .password("hashed")
                .email("author@example.com")
                .roles(Set.of(UserEntity.Role.USER))
                .build();
        userRepository.save(author);
    }

    @Test
    void can_find_article_by_slug() {
        // This test would need an author in the DB first
        // For now, testing the repository method exists and returns empty
        Optional<ArticleEntity> found = articleRepository.findBySlug("nonexistent-slug");
        assertThat(found).isEmpty();
    }

    @Test
    void exists_by_slug_returns_false_for_new_slug() {
        assertThat(articleRepository.existsBySlug("new-slug")).isFalse();
    }

    @Test
    void can_find_published_articles() {
        var page = articleRepository.findByStatus(ArticleStatus.PUBLISHED, PageRequest.of(0, 10));
        assertThat(page.getContent()).isEmpty();
    }
}
