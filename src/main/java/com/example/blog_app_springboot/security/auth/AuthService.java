package com.example.blog_app_springboot.security.auth;

import com.example.blog_app_springboot.common.exceptions.BadRequestException;
import com.example.blog_app_springboot.common.exceptions.ResourceNotFoundException;
import com.example.blog_app_springboot.security.TokenBlacklistService;
import com.example.blog_app_springboot.security.jwt.JwtTokenProvider;
import com.example.blog_app_springboot.users.dtos.LoginUserRequest;
import com.example.blog_app_springboot.users.entity.UserEntity;
import com.example.blog_app_springboot.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;

    @Transactional(readOnly = true)
    public AuthResponse login(LoginUserRequest request) {
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadRequestException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid username or password");
        }

        var roles = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername(), roles);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername(), roles);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(AuthResponse.UserDto.from(user))
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.isTokenValid(refreshToken)) {
            throw new BadRequestException("Invalid refresh token");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        var roles = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername(), roles);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername(), roles);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .user(AuthResponse.UserDto.from(user))
                .build();
    }

    @Transactional
    public void logout(String token) {
        tokenBlacklistService.blacklist(token);
    }
}
