package com.example.blog_app_springboot.analytics.entity;

import com.example.blog_app_springboot.articles.entity.ArticleEntity;
import com.example.blog_app_springboot.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "article_views",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"article_id", "viewer_ip", "viewed_at"}
        ))
public class ArticleViewEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private ArticleEntity article;

    @Column(name = "viewer_ip", nullable = false, length = 45)
    private String viewerIp;

    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt;
}
