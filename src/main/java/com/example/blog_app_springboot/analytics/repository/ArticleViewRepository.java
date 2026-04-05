package com.example.blog_app_springboot.analytics.repository;

import com.example.blog_app_springboot.analytics.entity.ArticleViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ArticleViewRepository extends JpaRepository<ArticleViewEntity, Long> {

    long countByArticleId(Long articleId);

    boolean existsByArticleIdAndViewerIpAndViewedAtAfter(
            Long articleId, String viewerIp, LocalDateTime viewedAtAfter);

    @Query("SELECT av.article.id, COUNT(av) as viewCount " +
            "FROM ArticleViewEntity av " +
            "GROUP BY av.article.id " +
            "ORDER BY viewCount DESC")
    List<Object[]> countViewsGroupedByArticle();

    @Query("SELECT COUNT(av) > 0 FROM ArticleViewEntity av " +
            "WHERE av.article.id = :articleId " +
            "AND av.viewerIp = :viewerIp " +
            "AND av.viewedAt > :since")
    boolean hasRecentView(@Param("articleId") Long articleId,
                          @Param("viewerIp") String viewerIp,
                          @Param("since") LocalDateTime since);
}
