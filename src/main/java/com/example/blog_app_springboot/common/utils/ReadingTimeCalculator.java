package com.example.blog_app_springboot.common.utils;

import org.springframework.stereotype.Component;

@Component
public class ReadingTimeCalculator {

    private static final int WORDS_PER_MINUTE = 200;

    public int calculate(String body) {
        if (body == null || body.isBlank()) {
            return 0;
        }
        int wordCount = body.trim().split("\\s+").length;
        return Math.max(1, (int) Math.ceil((double) wordCount / WORDS_PER_MINUTE));
    }
}
