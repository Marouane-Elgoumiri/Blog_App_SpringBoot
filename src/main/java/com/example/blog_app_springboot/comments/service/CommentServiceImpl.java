package com.example.blog_app_springboot.comments.service;

import com.example.blog_app_springboot.articles.repository.ArticleRepository;
import com.example.blog_app_springboot.comments.dtos.CommentResponse;
import com.example.blog_app_springboot.comments.dtos.CreateCommentRequest;
import com.example.blog_app_springboot.comments.entity.CommentEntity;
import com.example.blog_app_springboot.comments.repository.CommentRepository;
import com.example.blog_app_springboot.common.exceptions.ResourceNotFoundException;
import com.example.blog_app_springboot.common.exceptions.UnauthorizedException;
import com.example.blog_app_springboot.notifications.events.CommentCreatedEvent;
import com.example.blog_app_springboot.users.entity.UserEntity;
import com.example.blog_app_springboot.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public CommentResponse createComment(String articleSlug, CreateCommentRequest request, Long authorId) {
        var article = articleRepository.findBySlug(articleSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "slug", articleSlug));
        var author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", authorId));

        var comment = CommentEntity.builder()
                .title(request.getTitle())
                .body(request.getBody())
                .article(article)
                .author(author)
                .build();

        if (request.getParentId() != null) {
            var parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", request.getParentId()));
            comment.setParent(parent);
        }

        var savedComment = commentRepository.save(comment);

        eventPublisher.publishEvent(new CommentCreatedEvent(
                this,
                article.getId(),
                article.getSlug(),
                article.getTitle(),
                author.getId(),
                author.getUsername(),
                request.getBody()
        ));

        return toResponse(savedComment, true);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByArticle(String articleSlug) {
        var article = articleRepository.findBySlug(articleSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "slug", articleSlug));
        var comments = commentRepository.findByArticleIdAndParentIsNullOrderByCreatedAtDesc(article.getId());
        return comments.stream()
                .map(c -> toResponse(c, true))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getReplies(Long commentId) {
        var replies = commentRepository.findByParentIdOrderByCreatedAtAsc(commentId);
        return replies.stream()
                .map(r -> toResponse(r, false))
                .toList();
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        var comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new UnauthorizedException("You can only delete your own comments");
        }

        commentRepository.delete(comment);
    }

    private CommentResponse toResponse(CommentEntity comment, boolean includeReplies) {
        var builder = CommentResponse.builder()
                .id(comment.getId())
                .title(comment.getTitle())
                .body(comment.getBody())
                .createdAt(comment.getCreatedAt())
                .author(CommentResponse.CommentAuthor.builder()
                        .id(comment.getAuthor().getId())
                        .username(comment.getAuthor().getUsername())
                        .image(comment.getAuthor().getImage())
                        .build());

        if (includeReplies) {
            var replies = commentRepository.findByParentIdOrderByCreatedAtAsc(comment.getId());
            builder.replies(replies.stream()
                    .map(r -> toResponse(r, false))
                    .toList());
        }

        return builder.build();
    }
}
