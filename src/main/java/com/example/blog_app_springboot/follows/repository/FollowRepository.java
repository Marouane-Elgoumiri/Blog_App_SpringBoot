package com.example.blog_app_springboot.follows.repository;

import com.example.blog_app_springboot.follows.entity.FollowEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<FollowEntity, Long> {
    Optional<FollowEntity> findByFollowerIdAndFollowingId(Long followerId, Long followingId);
    List<FollowEntity> findByFollowerId(Long followerId);
    long countByFollowingId(Long followingId);
    long countByFollowerId(Long followerId);
    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);
    void deleteByFollowerIdAndFollowingId(Long followerId, Long followingId);
}
