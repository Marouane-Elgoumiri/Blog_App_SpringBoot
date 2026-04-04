package com.example.blog_app_springboot.users.service;

import com.example.blog_app_springboot.articles.entity.ArticleStatus;
import com.example.blog_app_springboot.articles.repository.ArticleRepository;
import com.example.blog_app_springboot.comments.repository.CommentRepository;
import com.example.blog_app_springboot.common.exceptions.DuplicateResourceException;
import com.example.blog_app_springboot.common.exceptions.ResourceNotFoundException;
import com.example.blog_app_springboot.follows.repository.FollowRepository;
import com.example.blog_app_springboot.interactions.repository.LikeRepository;
import com.example.blog_app_springboot.users.dtos.CreateUserRequest;
import com.example.blog_app_springboot.users.dtos.UserResponse;
import com.example.blog_app_springboot.users.dtos.UserStatsResponse;
import com.example.blog_app_springboot.users.entity.UserEntity;
import com.example.blog_app_springboot.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FollowRepository followRepository;
    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already exists: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }

        var user = UserEntity.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .build();

        var savedUser = userRepository.save(user);
        return UserResponse.from(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        long followerCount = followRepository.countByFollowingId(id);
        long followingCount = followRepository.countByFollowerId(id);
        return UserResponse.from(user, followerCount, followingCount);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        long followerCount = followRepository.countByFollowingId(user.getId());
        long followingCount = followRepository.countByFollowerId(user.getId());
        return UserResponse.from(user, followerCount, followingCount);
    }

    @Transactional(readOnly = true)
    public UserStatsResponse getUserStats(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        long totalArticles = articleRepository.countByAuthorId(userId);
        long publishedArticles = articleRepository.countByAuthorIdAndStatus(userId, ArticleStatus.PUBLISHED);
        long draftArticles = articleRepository.countByAuthorIdAndStatus(userId, ArticleStatus.DRAFT);
        long totalComments = commentRepository.countByAuthorId(userId);
        long totalFollowers = followRepository.countByFollowingId(userId);
        long totalFollowing = followRepository.countByFollowerId(userId);

        return UserStatsResponse.builder()
                .totalArticles(totalArticles)
                .publishedArticles(publishedArticles)
                .draftArticles(draftArticles)
                .totalComments(totalComments)
                .totalFollowers(totalFollowers)
                .totalFollowing(totalFollowing)
                .build();
    }
}
