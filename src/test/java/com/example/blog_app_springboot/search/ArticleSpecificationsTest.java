package com.example.blog_app_springboot.search;

import com.example.blog_app_springboot.articles.entity.ArticleEntity;
import com.example.blog_app_springboot.articles.entity.ArticleStatus;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;

class ArticleSpecificationsTest {

    @Test
    void has_status_returns_null_for_null_input() {
        assertThat(ArticleSpecifications.hasStatus(null)).isNull();
    }

    @Test
    void has_status_returns_spec_for_valid_input() {
        Specification<ArticleEntity> spec = ArticleSpecifications.hasStatus(ArticleStatus.PUBLISHED);
        assertThat(spec).isNotNull();
    }

    @Test
    void title_contains_returns_null_for_null_input() {
        assertThat(ArticleSpecifications.titleContains(null)).isNull();
    }

    @Test
    void title_contains_returns_null_for_blank_input() {
        assertThat(ArticleSpecifications.titleContains("")).isNull();
    }

    @Test
    void title_contains_returns_spec_for_valid_input() {
        Specification<ArticleEntity> spec = ArticleSpecifications.titleContains("spring");
        assertThat(spec).isNotNull();
    }

    @Test
    void has_tag_returns_null_for_null_input() {
        assertThat(ArticleSpecifications.hasTag(null)).isNull();
    }

    @Test
    void has_tag_returns_null_for_blank_input() {
        assertThat(ArticleSpecifications.hasTag("")).isNull();
    }

    @Test
    void has_tag_returns_spec_for_valid_input() {
        Specification<ArticleEntity> spec = ArticleSpecifications.hasTag("java");
        assertThat(spec).isNotNull();
    }

    @Test
    void has_author_returns_null_for_null_input() {
        assertThat(ArticleSpecifications.hasAuthor(null)).isNull();
    }

    @Test
    void has_author_returns_null_for_blank_input() {
        assertThat(ArticleSpecifications.hasAuthor("")).isNull();
    }

    @Test
    void has_author_returns_spec_for_valid_input() {
        Specification<ArticleEntity> spec = ArticleSpecifications.hasAuthor("john");
        assertThat(spec).isNotNull();
    }
}
