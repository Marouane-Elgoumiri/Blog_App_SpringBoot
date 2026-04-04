package com.example.blog_app_springboot.tags.service;

import com.example.blog_app_springboot.common.exceptions.DuplicateResourceException;
import com.example.blog_app_springboot.common.exceptions.ResourceNotFoundException;
import com.example.blog_app_springboot.common.utils.SlugGenerator;
import com.example.blog_app_springboot.tags.dtos.TagResponse;
import com.example.blog_app_springboot.tags.entity.TagEntity;
import com.example.blog_app_springboot.tags.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final SlugGenerator slugGenerator;

    @Transactional(readOnly = true)
    public List<TagResponse> getAllTags() {
        return tagRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TagResponse getTagBySlug(String slug) {
        var tag = tagRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", "slug", slug));
        return toResponse(tag);
    }

    @Transactional
    public TagResponse createTag(String name) {
        String normalizedName = name.toLowerCase().trim();

        if (tagRepository.existsByName(normalizedName)) {
            throw new DuplicateResourceException("Tag already exists: " + normalizedName);
        }

        String slug = slugGenerator.generate(normalizedName);

        var tag = TagEntity.builder()
                .name(normalizedName)
                .slug(slug)
                .build();

        var savedTag = tagRepository.save(tag);
        return toResponse(savedTag);
    }

    @Transactional
    public void deleteTag(Long tagId) {
        var tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", "id", tagId));
        tagRepository.delete(tag);
    }

    @Transactional(readOnly = true)
    public List<TagEntity> findOrCreateTags(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return List.of();
        }

        var existingTags = tagRepository.findByNameInIgnoreCase(
                tagNames.stream().map(String::toLowerCase).toList());

        var existingNames = existingTags.stream()
                .map(TagEntity::getName)
                .toList();

        var newTags = tagNames.stream()
                .filter(name -> !existingNames.contains(name.toLowerCase()))
                .map(name -> TagEntity.builder()
                        .name(name.toLowerCase().trim())
                        .slug(slugGenerator.generate(name.toLowerCase().trim()))
                        .build())
                .toList();

        if (!newTags.isEmpty()) {
            tagRepository.saveAll(newTags);
        }

        var result = new java.util.ArrayList<>(existingTags);
        result.addAll(newTags);
        return result;
    }

    private TagResponse toResponse(TagEntity tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .slug(tag.getSlug())
                .build();
    }
}
