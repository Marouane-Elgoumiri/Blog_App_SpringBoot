package com.example.blog_app_springboot.tags.repository;

import com.example.blog_app_springboot.tags.entity.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<TagEntity, Long> {
    Optional<TagEntity> findBySlug(String slug);
    Optional<TagEntity> findByName(String name);
    List<TagEntity> findByNameInIgnoreCase(List<String> names);
    boolean existsByName(String name);
}
