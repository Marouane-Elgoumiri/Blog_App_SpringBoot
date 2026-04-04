package com.example.blog_app_springboot.tags.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TagResponse {
    private Long id;
    private String name;
    private String slug;
}
