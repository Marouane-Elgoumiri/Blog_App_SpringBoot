package com.example.blog_app_springboot.comments.repository;

import com.example.blog_app_springboot.comments.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    List<CommentEntity> findByArticleIdAndParentIsNullOrderByCreatedAtDesc(Long articleId);
    List<CommentEntity> findByParentIdOrderByCreatedAtAsc(Long parentId);
    long countByAuthorId(Long authorId);
}
