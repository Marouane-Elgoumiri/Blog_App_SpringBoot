package com.example.blog_app_springboot.interactions.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InteractionResponse {
    private boolean action;
    private long count;
}
