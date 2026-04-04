package com.example.blog_app_springboot.notifications.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.mail.host")
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendCommentNotification(String to, String articleTitle, String commenterName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("New comment on \"" + articleTitle + "\"");
        message.setText(commenterName + " commented on your article: " + articleTitle);
        mailSender.send(message);
        log.info("Sent comment notification email to {}", to);
    }

    @Async
    public void sendFollowNotification(String to, String followerName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("New follower: " + followerName);
        message.setText(followerName + " started following you!");
        mailSender.send(message);
        log.info("Sent follow notification email to {}", to);
    }
}
