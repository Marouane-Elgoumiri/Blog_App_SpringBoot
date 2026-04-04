package com.example.blog_app_springboot.common.utils;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@Component
public class SlugGenerator {

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern DUPLICATED_DASHES = Pattern.compile("-{2,}");

    public String generate(String input) {
        if (input == null) return "";

        String slug = input.toLowerCase();
        slug = Normalizer.normalize(slug, Normalizer.Form.NFD);
        slug = WHITESPACE.matcher(slug).replaceAll("-");
        slug = NON_LATIN.matcher(slug).replaceAll("");
        slug = slug.replaceAll("--+", "-");
        slug = slug.replaceAll("^-|-$", "");

        return slug;
    }

    public String generateUniqueSlug(String input, Predicate<String> existsCheck) {
        String baseSlug = generate(input);
        String uniqueSlug = baseSlug;
        int counter = 1;

        while (existsCheck.test(uniqueSlug)) {
            uniqueSlug = baseSlug + "-" + counter;
            counter++;
        }

        return uniqueSlug;
    }
}
