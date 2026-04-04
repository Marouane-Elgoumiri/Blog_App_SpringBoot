package com.example.blog_app_springboot.users;

import com.example.blog_app_springboot.common.exceptions.DuplicateResourceException;
import com.example.blog_app_springboot.common.exceptions.ResourceNotFoundException;
import com.example.blog_app_springboot.users.dtos.CreateUserRequest;
import com.example.blog_app_springboot.users.dtos.UserResponse;
import com.example.blog_app_springboot.users.entity.UserEntity;
import com.example.blog_app_springboot.follows.repository.FollowRepository;
import com.example.blog_app_springboot.users.repository.UserRepository;
import com.example.blog_app_springboot.users.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private FollowRepository followRepository;

    @InjectMocks
    private UserService userService;

    private UserEntity sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = UserEntity.builder()
                .id(1L)
                .username("testuser")
                .password("hashedPassword")
                .email("test@example.com")
                .roles(Set.of(UserEntity.Role.USER))
                .build();
    }

    @Test
    void can_create_user() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("newuser");
        request.setPassword("password123");
        request.setEmail("new@example.com");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(userRepository.save(any(UserEntity.class))).thenReturn(sampleUser);

        UserResponse response = userService.createUser(request);

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void throws_on_duplicate_username() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("existing");
        request.setPassword("password123");
        request.setEmail("new@example.com");

        when(userRepository.existsByUsername("existing")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Username already exists");
    }

    @Test
    void throws_on_duplicate_email() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("newuser");
        request.setPassword("password123");
        request.setEmail("existing@example.com");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    void can_get_user_by_id() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(followRepository.countByFollowingId(1L)).thenReturn(0L);
        when(followRepository.countByFollowerId(1L)).thenReturn(0L);

        UserResponse response = userService.getUserById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("testuser");
    }

    @Test
    void throws_on_user_not_found() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void can_get_user_by_username() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(sampleUser));
        when(followRepository.countByFollowingId(1L)).thenReturn(0L);
        when(followRepository.countByFollowerId(1L)).thenReturn(0L);

        UserResponse response = userService.getUserByUsername("testuser");

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("testuser");
    }
}
