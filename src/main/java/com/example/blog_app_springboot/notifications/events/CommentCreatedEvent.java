package com.example.blog_app_springboot.notifications.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class CommentCreatedEvent extends ApplicationEvent {

    private final Long articleId;
    private final String articleSlug;
    private final String articleTitle;
    private final Long authorId;
    private final String authorUsername;
    private final String commentBody;

    public CommentCreatedEvent(Object source, Long articleId, String articleSlug,
                               String articleTitle, Long authorId, String authorUsername,
                               String commentBody) {
        super(source);
        this.articleId = articleId;
        this.articleSlug = articleSlug;
        this.articleTitle = articleTitle;
        this.authorId = authorId;
        this.authorUsername = authorUsername;
        this.commentBody = commentBody;
    }
}
