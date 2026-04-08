package com.example.blog_app_springboot.users.controller;

import com.example.blog_app_springboot.common.dtos.ApiResponse;
import com.example.blog_app_springboot.security.util.SecurityUtil;
import com.example.blog_app_springboot.users.dtos.CreateUserRequest;
import com.example.blog_app_springboot.users.dtos.UpdateUserRequest;
import com.example.blog_app_springboot.users.dtos.UserResponse;
import com.example.blog_app_springboot.users.dtos.UserStatsResponse;
import com.example.blog_app_springboot.users.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UsersController {

	private final UserService userService;
	private final SecurityUtil securityUtil;

	@PostMapping
	public ResponseEntity<ApiResponse<UserResponse>> signup(@Valid @RequestBody CreateUserRequest request) {
		var response = userService.createUser(request);
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(ApiResponse.success("User created successfully", response));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long id) {
		var response = userService.getUserById(id);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping("/me")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<ApiResponse<UserResponse>> getMe() {
		var response = userService.getUserById(securityUtil.getCurrentUserId());
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@PutMapping("/me")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<ApiResponse<UserResponse>> updateMe(@Valid @RequestBody UpdateUserRequest request) {
		var response = userService.updateUser(securityUtil.getCurrentUserId(), request);
		return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
	}

	@GetMapping("/me/stats")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<ApiResponse<UserStatsResponse>> getMyStats() {
		var stats = userService.getUserStats(securityUtil.getCurrentUserId());
		return ResponseEntity.ok(ApiResponse.success(stats));
	}
}
