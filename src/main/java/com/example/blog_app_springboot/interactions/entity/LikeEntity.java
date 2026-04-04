package com.example.blog_app_springboot.interactions.entity;

import com.example.blog_app_springboot.articles.entity.ArticleEntity;
import com.example.blog_app_springboot.common.base.BaseEntity;
import com.example.blog_app_springboot.users.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "likes",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_article_like", columnNames = {"user_id", "article_id"})
    },
    indexes = {
        @Index(name = "idx_like_article", columnList = "article_id"),
        @Index(name = "idx_like_user", columnList = "user_id")
    }
)
public class LikeEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private ArticleEntity article;
}
