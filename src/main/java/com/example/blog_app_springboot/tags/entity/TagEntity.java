package com.example.blog_app_springboot.tags.entity;

import com.example.blog_app_springboot.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "tags", indexes = {
    @Index(name = "idx_tag_name", columnList = "name"),
    @Index(name = "idx_tag_slug", columnList = "slug")
})
public class TagEntity extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;
}
