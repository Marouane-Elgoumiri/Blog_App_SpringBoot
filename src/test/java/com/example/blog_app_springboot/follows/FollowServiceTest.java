package com.example.blog_app_springboot.follows;

import com.example.blog_app_springboot.common.exceptions.BadRequestException;
import com.example.blog_app_springboot.common.exceptions.ResourceNotFoundException;
import com.example.blog_app_springboot.follows.entity.FollowEntity;
import com.example.blog_app_springboot.follows.repository.FollowRepository;
import com.example.blog_app_springboot.follows.service.FollowService;
import com.example.blog_app_springboot.users.entity.UserEntity;
import com.example.blog_app_springboot.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

    @Mock
    private FollowRepository followRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private FollowService followService;

    private UserEntity follower;
    private UserEntity following;

    @BeforeEach
    void setUp() {
        follower = UserEntity.builder()
                .id(1L)
                .username("follower")
                .password("hashed")
                .email("follower@example.com")
                .roles(Set.of(UserEntity.Role.USER))
                .build();

        following = UserEntity.builder()
                .id(2L)
                .username("following")
                .password("hashed")
                .email("following@example.com")
                .roles(Set.of(UserEntity.Role.USER))
                .build();
    }

    @Test
    void can_toggle_follow_creates_follow() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(follower));
        when(userRepository.findById(2L)).thenReturn(Optional.of(following));
        when(followRepository.findByFollowerIdAndFollowingId(1L, 2L)).thenReturn(Optional.empty());
        when(followRepository.countByFollowingId(2L)).thenReturn(1L);

        var result = followService.toggleFollow(2L, 1L);

        assertThat(result.following()).isTrue();
        assertThat(result.count()).isEqualTo(1L);
        verify(followRepository).save(any(FollowEntity.class));
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    void can_toggle_follow_removes_existing_follow() {
        var existingFollow = FollowEntity.builder()
                .id(1L)
                .follower(follower)
                .following(following)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(follower));
        when(userRepository.findById(2L)).thenReturn(Optional.of(following));
        when(followRepository.findByFollowerIdAndFollowingId(1L, 2L)).thenReturn(Optional.of(existingFollow));
        when(followRepository.countByFollowingId(2L)).thenReturn(0L);

        var result = followService.toggleFollow(2L, 1L);

        assertThat(result.following()).isFalse();
        assertThat(result.count()).isEqualTo(0L);
        verify(followRepository).delete(existingFollow);
    }

    @Test
    void throws_when_following_self() {
        assertThatThrownBy(() -> followService.toggleFollow(1L, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("cannot follow yourself");
    }

    @Test
    void throws_when_following_nonexistent_user() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(follower));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> followService.toggleFollow(999L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void can_check_is_following() {
        when(followRepository.existsByFollowerIdAndFollowingId(1L, 2L)).thenReturn(true);

        assertThat(followService.isFollowing(2L, 1L)).isTrue();
    }

    @Test
    void can_get_follower_count() {
        when(followRepository.countByFollowingId(2L)).thenReturn(10L);

        assertThat(followService.getFollowerCount(2L)).isEqualTo(10L);
    }

    @Test
    void can_get_following_count() {
        when(followRepository.countByFollowerId(1L)).thenReturn(5L);

        assertThat(followService.getFollowingCount(1L)).isEqualTo(5L);
    }
}
