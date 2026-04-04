package com.example.blog_app_springboot.follows.controller;

import com.example.blog_app_springboot.common.dtos.ApiResponse;
import com.example.blog_app_springboot.follows.service.FollowService;
import com.example.blog_app_springboot.security.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;
    private final SecurityUtil securityUtil;

    @PostMapping("/{id}/follow")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<FollowService.FollowResponse>> toggleFollow(@PathVariable Long id) {
        var result = followService.toggleFollow(id, securityUtil.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success("Follow toggled", result));
    }
}
