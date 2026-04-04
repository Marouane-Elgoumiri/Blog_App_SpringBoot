package com.example.blog_app_springboot.follows.service;

import com.example.blog_app_springboot.common.exceptions.BadRequestException;
import com.example.blog_app_springboot.common.exceptions.ResourceNotFoundException;
import com.example.blog_app_springboot.follows.entity.FollowEntity;
import com.example.blog_app_springboot.follows.repository.FollowRepository;
import com.example.blog_app_springboot.notifications.events.UserFollowedEvent;
import com.example.blog_app_springboot.users.entity.UserEntity;
import com.example.blog_app_springboot.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public FollowResponse toggleFollow(Long followingId, Long followerId) {
        if (followingId.equals(followerId)) {
            throw new BadRequestException("You cannot follow yourself");
        }

        var follower = userRepository.findById(followerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", followerId));
        var following = userRepository.findById(followingId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", followingId));

        var existingFollow = followRepository.findByFollowerIdAndFollowingId(followerId, followingId);

        if (existingFollow.isPresent()) {
            followRepository.delete(existingFollow.get());
            long count = followRepository.countByFollowingId(followingId);
            return new FollowResponse(false, count);
        } else {
            var follow = FollowEntity.builder()
                    .follower(follower)
                    .following(following)
                    .build();
            followRepository.save(follow);

            eventPublisher.publishEvent(new UserFollowedEvent(
                    this,
                    follower.getId(),
                    follower.getUsername(),
                    following.getId(),
                    following.getUsername()
            ));

            long count = followRepository.countByFollowingId(followingId);
            return new FollowResponse(true, count);
        }
    }

    @Transactional(readOnly = true)
    public boolean isFollowing(Long followingId, Long followerId) {
        return followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
    }

    @Transactional(readOnly = true)
    public long getFollowerCount(Long userId) {
        return followRepository.countByFollowingId(userId);
    }

    @Transactional(readOnly = true)
    public long getFollowingCount(Long userId) {
        return followRepository.countByFollowerId(userId);
    }

    public record FollowResponse(boolean following, long count) {}
}
