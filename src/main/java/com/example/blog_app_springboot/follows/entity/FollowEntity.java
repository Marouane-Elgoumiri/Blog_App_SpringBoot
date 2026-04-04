package com.example.blog_app_springboot.follows.entity;

import com.example.blog_app_springboot.common.base.BaseEntity;
import com.example.blog_app_springboot.users.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "follows",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_follow", columnNames = {"follower_id", "following_id"})
    },
    indexes = {
        @Index(name = "idx_follow_follower", columnList = "follower_id"),
        @Index(name = "idx_follow_following", columnList = "following_id")
    }
)
public class FollowEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    private UserEntity follower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id", nullable = false)
    private UserEntity following;
}
