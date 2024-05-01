package com.example.blog_app_springboot.comments;

import org.springframework.stereotype.Service;

@Service
public class CommentService {
    private CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }
}
