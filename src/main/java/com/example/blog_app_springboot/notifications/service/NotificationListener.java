package com.example.blog_app_springboot.notifications.service;

import com.example.blog_app_springboot.notifications.events.CommentCreatedEvent;
import com.example.blog_app_springboot.notifications.events.UserFollowedEvent;
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

    @Async
    @EventListener
    public void onCommentCreated(CommentCreatedEvent event) {
        log.info("New comment on article '{}' by {}: {}",
                event.getArticleTitle(), event.getAuthorUsername(), event.getCommentBody());
        if (emailService != null) {
            // TODO: Send email to article author
            // emailService.sendCommentNotification(event.getArticleAuthorEmail(), ...);
        }
    }

    @Async
    @EventListener
    public void onUserFollowed(UserFollowedEvent event) {
        log.info("User '{}' started following '{}'",
                event.getFollowerUsername(), event.getFollowingUsername());
        if (emailService != null) {
            // TODO: Send email to followed user
            // emailService.sendFollowNotification(event.getFollowingEmail(), ...);
        }
    }
}
