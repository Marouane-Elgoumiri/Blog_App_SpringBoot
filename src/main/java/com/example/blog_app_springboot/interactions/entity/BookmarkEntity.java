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
@Table(name = "bookmarks", uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_article_bookmark", columnNames = {"user_id", "article_id"})
})
public class BookmarkEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private ArticleEntity article;
}
