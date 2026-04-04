package com.example.blog_app_springboot.search;

import com.example.blog_app_springboot.articles.entity.ArticleEntity;
import com.example.blog_app_springboot.articles.entity.ArticleStatus;
import org.springframework.data.jpa.domain.Specification;

public class ArticleSpecifications {

    public static Specification<ArticleEntity> hasStatus(ArticleStatus status) {
        if (status == null) return null;
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<ArticleEntity> titleContains(String query) {
        if (query == null || query.isBlank()) return null;
        return (root, queryCb, cb) -> cb.like(cb.lower(root.get("title")), "%" + query.toLowerCase() + "%");
    }

    public static Specification<ArticleEntity> hasTag(String tagName) {
        if (tagName == null || tagName.isBlank()) return null;
        return (root, query, cb) -> cb.like(cb.lower(root.join("tags").get("name")), tagName.toLowerCase());
    }

    public static Specification<ArticleEntity> hasAuthor(String authorName) {
        if (authorName == null || authorName.isBlank()) return null;
        return (root, query, cb) -> cb.like(cb.lower(root.get("author").get("username")), "%" + authorName.toLowerCase() + "%");
    }
}
