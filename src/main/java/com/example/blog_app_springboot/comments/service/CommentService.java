package com.example.blog_app_springboot.comments.service;

import com.example.blog_app_springboot.comments.dtos.CommentResponse;
import com.example.blog_app_springboot.comments.dtos.CreateCommentRequest;

import java.util.List;

public interface CommentService {
    CommentResponse createComment(String articleSlug, CreateCommentRequest request, Long authorId);
    List<CommentResponse> getCommentsByArticle(String articleSlug);
    List<CommentResponse> getReplies(Long commentId);
    void deleteComment(Long commentId, Long userId);
}
