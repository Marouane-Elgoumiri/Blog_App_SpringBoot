package com.example.blog_app_springboot.notifications.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserFollowedEvent extends ApplicationEvent {

    private final Long followerId;
    private final String followerUsername;
    private final Long followingId;
    private final String followingUsername;

    public UserFollowedEvent(Object source, Long followerId, String followerUsername,
                             Long followingId, String followingUsername) {
        super(source);
        this.followerId = followerId;
        this.followerUsername = followerUsername;
        this.followingId = followingId;
        this.followingUsername = followingUsername;
    }
}
