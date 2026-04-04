package com.example.blog_app_springboot.comments.controller;

import com.example.blog_app_springboot.comments.dtos.CommentResponse;
import com.example.blog_app_springboot.comments.dtos.CreateCommentRequest;
import com.example.blog_app_springboot.comments.service.CommentService;
import com.example.blog_app_springboot.common.dtos.ApiResponse;
import com.example.blog_app_springboot.security.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/articles/{slug}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final SecurityUtil securityUtil;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @PathVariable String slug,
            @Valid @RequestBody CreateCommentRequest request) {
        var response = commentService.createComment(slug, request, securityUtil.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Comment created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(@PathVariable String slug) {
        var response = commentService.getCommentsByArticle(slug);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{commentId}/replies")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getReplies(@PathVariable Long commentId) {
        var response = commentService.getReplies(commentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId, securityUtil.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }
}
