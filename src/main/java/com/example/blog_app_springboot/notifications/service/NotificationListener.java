package com.example.blog_app_springboot.notifications.service;

import com.example.blog_app_springboot.articles.repository.ArticleRepository;
import com.example.blog_app_springboot.notifications.events.CommentCreatedEvent;
import com.example.blog_app_springboot.notifications.events.UserFollowedEvent;
import com.example.blog_app_springboot.users.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationListener {

    @Autowired(required = false)
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Async
    @EventListener
    public void onCommentCreated(CommentCreatedEvent event) {
        log.info("New comment on article '{}' by {}: {}",
                event.getArticleTitle(), event.getAuthorUsername(), event.getCommentBody());

        if (emailService != null) {
            articleRepository.findById(event.getArticleId()).ifPresent(article -> {
                userRepository.findById(article.getAuthor().getId()).ifPresent(author -> {
                    emailService.sendCommentNotification(
                            author.getEmail(),
                            event.getArticleTitle(),
                            event.getAuthorUsername()
                    );
                });
            });
        }
    }

    @Async
    @EventListener
    public void onUserFollowed(UserFollowedEvent event) {
        log.info("User '{}' started following '{}'",
                event.getFollowerUsername(), event.getFollowingUsername());

        if (emailService != null) {
            userRepository.findById(event.getFollowingId()).ifPresent(followedUser -> {
                emailService.sendFollowNotification(
                        followedUser.getEmail(),
                        event.getFollowerUsername()
                );
            });
        }
    }
}
