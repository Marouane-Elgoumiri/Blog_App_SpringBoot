package com.example.blog_app_springboot.articles.entity;

import com.example.blog_app_springboot.common.base.BaseEntity;
import com.example.blog_app_springboot.tags.entity.TagEntity;
import com.example.blog_app_springboot.users.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "articles", indexes = {
    @Index(name = "idx_article_slug", columnList = "slug"),
    @Index(name = "idx_article_status", columnList = "status"),
    @Index(name = "idx_article_author", columnList = "author_id"),
    @Index(name = "idx_article_created_at", columnList = "created_at")
})
public class ArticleEntity extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String slug;

    private String subtitle;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ArticleStatus status = ArticleStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private UserEntity author;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "article_tags",
            joinColumns = @JoinColumn(name = "article_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<TagEntity> tags = new HashSet<>();
}
