package com.example.blog_app_springboot.users;

import com.example.blog_app_springboot.config.JpaConfig;
import com.example.blog_app_springboot.users.entity.UserEntity;
import com.example.blog_app_springboot.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void can_create_user() {
        var user = UserEntity.builder()
                .username("testuser")
                .password("hashedPassword")
                .email("test@example.com")
                .build();
        userRepository.save(user);

        Optional<UserEntity> found = userRepository.findByUsername("testuser");
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void can_find_user_by_username() {
        var user = UserEntity.builder()
                .username("findme")
                .password("hashedPassword")
                .email("findme@example.com")
                .build();
        userRepository.save(user);

        Optional<UserEntity> found = userRepository.findByUsername("findme");
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("findme");
    }

    @Test
    void exists_by_username_returns_true() {
        var user = UserEntity.builder()
                .username("existsuser")
                .password("hashedPassword")
                .email("exists@example.com")
                .build();
        userRepository.save(user);

        assertThat(userRepository.existsByUsername("existsuser")).isTrue();
        assertThat(userRepository.existsByUsername("nonexistent")).isFalse();
    }

    @Test
    void exists_by_email_returns_true() {
        var user = UserEntity.builder()
                .username("emailuser")
                .password("hashedPassword")
                .email("unique@example.com")
                .build();
        userRepository.save(user);

        assertThat(userRepository.existsByEmail("unique@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("other@example.com")).isFalse();
    }
}
