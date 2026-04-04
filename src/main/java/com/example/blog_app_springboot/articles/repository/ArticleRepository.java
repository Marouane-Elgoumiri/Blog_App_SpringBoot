package com.example.blog_app_springboot.articles.repository;

import com.example.blog_app_springboot.articles.entity.ArticleEntity;
import com.example.blog_app_springboot.articles.entity.ArticleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<ArticleEntity, Long>, JpaSpecificationExecutor<ArticleEntity> {
    Optional<ArticleEntity> findBySlug(String slug);
    boolean existsBySlug(String slug);
    Page<ArticleEntity> findByStatus(ArticleStatus status, Pageable pageable);

    @Query(value = """
            SELECT a.* FROM articles a
            WHERE a.status = 'PUBLISHED'
            AND (
                to_tsvector('english', a.title || ' ' || a.body) @@ plainto_tsquery('english', :query)
                OR a.title ILIKE %:query%
                OR a.body ILIKE %:query%
            )
            ORDER BY a.created_at DESC
            """,
            countQuery = """
            SELECT COUNT(*) FROM articles a
            WHERE a.status = 'PUBLISHED'
            AND (
                to_tsvector('english', a.title || ' ' || a.body) @@ plainto_tsquery('english', :query)
                OR a.title ILIKE %:query%
                OR a.body ILIKE %:query%
            )
            """,
            nativeQuery = true)
    Page<ArticleEntity> fullTextSearch(@Param("query") String query, Pageable pageable);

    long countByAuthorId(Long authorId);
    long countByAuthorIdAndStatus(Long authorId, ArticleStatus status);
}
